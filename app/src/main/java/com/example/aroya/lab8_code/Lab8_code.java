package com.example.aroya.lab8_code;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

public class Lab8_code extends AppCompatActivity {

    Button add;
    EditText name,birthday,gift;
    String nameValue,birthdayValue,giftValue;
    AlertDialog.Builder builder;
    RecyclerView recyclerView;
    MyDB myDB;
    ArrayList<Map<String,String>>dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        builder=new AlertDialog.Builder(this);
        myDB=new MyDB(this);
        dataBase=new ArrayList<>();
        loadDatas();
        loadMain();
    }

    private void loadMain(){
        setContentView(R.layout.lab8_code_main);

        add=(Button)findViewById(R.id.Add_item);
        recyclerView=(RecyclerView)findViewById(R.id.recycleView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyAdapter());

        add.setOnClickListener(new AddItem());
    }

    private void loadAdd(){
        setContentView(R.layout.add_confirm);
        add=(Button)findViewById(R.id.confirm_Add_item);
        name=(EditText)findViewById(R.id.confirm_nameEdit);
        birthday=(EditText)findViewById(R.id.confirm_birthdayEdit);
        gift=(EditText)findViewById(R.id.confirm_giftEdit);


        add.setOnClickListener(new AddItemConfirm());
    }

    private class AddItem implements View.OnClickListener{
        @Override
        public void onClick(View view){
            loadAdd();
        }
    }
    private class AddItemConfirm implements View.OnClickListener{
        @Override
        public void onClick(View view){
            nameValue=name.getText().toString();
            birthdayValue=birthday.getText().toString();
            giftValue=gift.getText().toString();
            if(nameValue.equals("")){
                Toast.makeText(getApplicationContext(),getString(R.string.name)+getString(R.string.blankToast),Toast.LENGTH_SHORT)
                .show();
            }
            else {
                myDB.insert(nameValue,birthdayValue,giftValue);
                loadDatas();
                loadMain();
            }
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

        //创建新View，被LayoutManager所调用
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item,viewGroup,false);
            ViewHolder vh = new ViewHolder(view);
            return vh;
        }


        //将数据与界面进行绑定的操作
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            viewHolder.item_name.setText(dataBase.get(position).get("name"));
            viewHolder.item_birthday.setText(dataBase.get(position).get("birthday"));
            viewHolder.item_gift.setText(dataBase.get(position).get("gift"));
        }
        //获取数据的数量
        @Override
        public int getItemCount() {
            return dataBase.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener,View.OnLongClickListener{
            public TextView item_name,item_birthday,item_gift;


            public ViewHolder(View view){
                super(view);
                item_name=(TextView)view.findViewById(R.id.item_name);
                item_birthday=(TextView)view.findViewById(R.id.item_birthday);
                item_gift=(TextView)view.findViewById(R.id.item_gift);
                view.setOnClickListener(this);
                view.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View view){
                //修改
                builder=new AlertDialog.Builder(Lab8_code.this);
                builder.setTitle("修改条目");

                LayoutInflater factory=LayoutInflater.from(Lab8_code.this);
                View builderView=factory.inflate(R.layout.builderlayout,null);

                final TextView thisName=(TextView)builderView.findViewById(R.id.builder_nameEdit);
                final EditText thisBirth=(EditText)builderView.findViewById(R.id.builder_birthdayEdit);
                final EditText thisGift=(EditText)builderView.findViewById(R.id.builder_giftEdit);
                final TextView thisPhone=(TextView)builderView.findViewById(R.id.builder_phonenum);
                thisName.setText(dataBase.get(getPosition()).get("name"));
                thisBirth.setText(dataBase.get(getPosition()).get("birthday"));
                thisGift.setText(dataBase.get(getPosition()).get("gift"));


                //电话
                //Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

                //int isHas = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                //检查权限
                requestContact();


                Cursor phone = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
                String Number = "";
                while(phone.moveToNext()) {
                    String id=phone.getString(phone.getColumnIndex("_id"));
                    if(phone.getString(phone.getColumnIndex("display_name"))
                            .equals(thisName.getText().toString())){
                        Cursor t=getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                "contact_id="+id,
                                null,
                                null
                        );
                        while(t.moveToNext()){
                            Number+=t.getString(t.getColumnIndex("data1"));
                        }
                        t.close();
                    }
                }
                phone.close();
                if(Number.equals(""))Number="null";
                thisPhone.setText(Number);

                builder.setView(builderView);

                builder.setPositiveButton("保存修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myDB.update(thisName.getText().toString(),thisBirth.getText().toString(),thisGift.getText().toString());
                        loadDatas();
                        notifyItemChanged(getPosition());
                    }
                });

                builder.setNegativeButton("放弃修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                builder.show();
            }
            @Override
            public boolean onLongClick(View view){
                builder=new AlertDialog.Builder(Lab8_code.this);
                builder.setMessage("是否删除？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myDB.delete(item_name.getText().toString());
                        dataBase.remove(getPosition());
                        notifyItemRemoved(getPosition());
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                builder.show();
                return true;
            }
        }
    }

    private class MyDB extends SQLiteOpenHelper{
        private static final String DB_NAME = "Contacts.db";
        private static final String TABLE_NAME = "Contacts";
        private static final int DB_VERSION = 1;

        public MyDB(Context context,String name,SQLiteDatabase.CursorFactory cursorFactory,
                         int version){
            super(context,name,cursorFactory,version);
        }
        public MyDB(Context context){
            this(context,DB_NAME,null,DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            String CREATE_TABLE = "create table " + TABLE_NAME
                    + " (_id integer primary key , "
                    + "name text , "
                    + "birth text , "
                    + "gift text);";
            db.execSQL(CREATE_TABLE);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // To Do
        }

        public void insert(String name, String birth, String gift) {
            try{
                SQLiteDatabase db = getWritableDatabase();
                db.execSQL("insert into "+TABLE_NAME+" (name,birth,gift)"
                +" values('"+name+"','"+birth+"','"+gift+"');");
                db.close();
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        e.toString(), Toast.LENGTH_SHORT)
                        .show();
            }
        }
        public void update(String name, String birth, String gift) {
            try{
                SQLiteDatabase db = getWritableDatabase();
                db.execSQL("update "+TABLE_NAME+" set"
                        +" birth ='"+birth+"',"
                        +" gift ='"+gift+"'"
                        +" where name='"+name+"';");
                db.close();
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        e.toString(), Toast.LENGTH_SHORT)
                        .show();
            }
        }
        public void delete(String name) {
            try{
                SQLiteDatabase db = getWritableDatabase();
                db.execSQL("delete from "+TABLE_NAME
                        +" where name='"+name+"';");
                db.close();
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        e.toString(), Toast.LENGTH_SHORT)
                        .show();
            }
        }
        public Cursor selectAll(){
            try{
                Cursor cursor;
                SQLiteDatabase db=getWritableDatabase();
                cursor=db.rawQuery("select * from "+TABLE_NAME+";",null);
                db.close();
                return cursor;
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        e.toString(), Toast.LENGTH_SHORT)
                        .show();
            }
            return null;
        }
    }
    private void loadDatas(){
        SQLiteDatabase db=myDB.getWritableDatabase();
        Cursor cursor=db.rawQuery("select * from "+MyDB.TABLE_NAME+";",null);
        dataBase.clear();
        while(cursor.moveToNext()){
            Map<String,String>temp=new HashMap<String,String>();
            temp.put("name",cursor.getString(1));
            temp.put("birthday",cursor.getString(2));
            temp.put("gift",cursor.getString(3));
            dataBase.add(temp);
        }
    }
    private void requestContact(){
        try{
            if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
                if(ContextCompat.checkSelfPermission(Lab8_code.this, android.Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(Lab8_code.this,
                            new String[]{android.Manifest.permission.READ_CONTACTS},
                            0);
                }

            }
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissons[],int[] grantResults){
        if(grantResults.length>0&&grantResults[0]==
            PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this,"权限申请成功",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this,"权限申请失败",Toast.LENGTH_SHORT).show();
        }
    }
}
