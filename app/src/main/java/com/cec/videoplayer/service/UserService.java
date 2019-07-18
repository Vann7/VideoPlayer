package com.cec.videoplayer.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.cec.videoplayer.model.User;
import com.cec.videoplayer.db.DatabaseHelper;
import com.cec.videoplayer.dto.UserDto;
import com.cec.videoplayer.utils.DtoUtils;

import org.litepal.LitePal;

import java.util.List;
import java.util.Random;

public class UserService {

    private Context mContext;
    private DatabaseHelper helper;
    private SQLiteDatabase db;

    public UserService(Context context) {
        mContext = context;
//        helper = DatabaseHelper.getInstance(mContext);
//        db = helper.getWritableDatabase();
//        db = LitePal.getDatabase();
    }

    public UserService(){}

    public void create() {

    }

    /**
     * 系统首次登录,插入root用户信息
     * @param user
     * @return
     */
    public boolean insert(User user) {
/*
        ContentValues values = new ContentValues();
        values.put("id",1);
        values.put("name","tony");
        values.put("password","123456");
        long l =  db.insert("user",null,values);*/
//        SQLiteDatabase db2 = LitePal.getDatabase();
        Random random = new Random();
        user.setId(random.nextInt(100));
//        user.setAppUpdate(true);
        user = ShiroService.md5(user);
        boolean flag =  user.save();
        return flag;
    }

    public List<User> checkUser(User user) {
//        user = ShiroService.shiroPwd(user);
        user = ShiroService.md5(user);
        List<User> list = LitePal.where("name = ? and password = ?",user.getName(),user.getPassword()).find(User.class);
        return list;
    }

    public List<User> getALl() {
        return LitePal.findAll(User.class);
    }

    public int updatePassword(User user) {
        ContentValues values = new ContentValues();
        values.put("password",user.getPassword());
       int flag =  LitePal.update(User.class, values, user.getId());
       return flag;
    }

    public void deleteAll() {
        LitePal.deleteAll(User.class);
    }

    public void batchInsert(List<UserDto> uList) {
        User root = new User("root","admin");
//        root = ShiroService.shiroPwd(root);
        root = ShiroService.md5(root);
        root.save();//保存root用户信息
        for (UserDto userDto : uList) {
            User user = DtoUtils.toUser(userDto);
            user.save();
        }
    }

}
