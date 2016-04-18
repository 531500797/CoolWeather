package ouc.lm.coolweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ouc.lm.coolweather.model.City;
import ouc.lm.coolweather.model.County;
import ouc.lm.coolweather.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lavida on 2016-04-14.
 */
public class CoolWeatherDB {
    //数据库名
    public static final String DB_NAME="cool_weather";
    //数据库版本
    public static final int VERSION=1;
    private static CoolWeatherDB coolWeatherDB;
    private SQLiteDatabase db;  //管理和操作SQLite数据库  Cursor保存查询返回的结果，提供随机读、写功能
    //将构造方法私有化
    private CoolWeatherDB(Context context){
        CoolWeatherOpenHelper dbHelper=new CoolWeatherOpenHelper(context,DB_NAME,null,VERSION);
        db=dbHelper.getWritableDatabase();  //创建一个读写数据库
    }
    //获取CoolWeatherDB的实例
    public synchronized static CoolWeatherDB getInstance(Context context){
        if (coolWeatherDB==null){
            coolWeatherDB=new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }
    //将Province实例存储到数据库
    public void saveProvince(Province province){
        if (province!=null){
            ContentValues values=new ContentValues();  //用来存储值
            values.put("province_name",province.getProvinceName());
            values.put("province_code",province.getPrivinceCode());
            db.insert("Province",null,values);//往数据库里插入一行数据
        }
    }
    //从数据库中读取全国省份的信息
    public List<Province> loadProvinces(){
        List<Province> list=new ArrayList<Province>();
        Cursor cursor=db.query("Province",null,null,null,null,null,null); //第一个为表的名字，也就是说查询该表中的所有数据
        if (cursor.moveToFirst()){   //转到查询结果的第一行
            do {
                Province province=new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));  //按照id进行索引
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setPrivinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                list.add(province);
            }while (cursor.moveToNext());
        }
        return list;
    }
    //将City实例存储到数据库
    public void saveCity(City city){
        if (city!=null){
            ContentValues values=new ContentValues();
            values.put("city_name",city.getCityName());
            values.put("city_code",city.getCityCode());
            values.put("province_id",city.getPrivinceId());
            db.insert("City",null,values);
        }
    }
    //从数据库里读取某个省得所有城市
    public List<City> loadCities(int provinceId) {
        List<City> list = new ArrayList<City>();
        Cursor cursor = db.query("City", null, "province_id=?", new String[]{String.valueOf(provinceId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
              //  city.setPrivinceId(provinceId);
                list.add(city);
            } while (cursor.moveToNext());
        }
        return list;
    }
    //将County实例存储到数据库
    public void saveCounty(County county){
        if (county!=null){
            ContentValues values=new ContentValues();
            values.put("county_name",county.getCountyName());
            values.put("county_code",county.getCountyCode());
            values.put("city_id",county.getCityId());
            db.insert("County",null,values);
        }
    }
    //从数据库中读取某城市下所有县的信息
    public List<County> loadCounties(int cityId){
        List<County> list=new ArrayList<County>();
        Cursor cursor=db.query("County",null,"city_id=?",new String[]{String.valueOf(cityId)},null,null,null);
        if (cursor.moveToFirst()){
            do {
                County county=new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
          //      county.setCityId(cityId);
                list.add(county);
            }while (cursor.moveToNext());
        }
        return list;
    }
}
