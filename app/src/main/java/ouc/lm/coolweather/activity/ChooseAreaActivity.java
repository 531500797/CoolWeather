package ouc.lm.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import ouc.lm.coolweather.db.CoolWeatherDB;
import ouc.lm.coolweather.model.City;
import ouc.lm.coolweather.model.County;
import ouc.lm.coolweather.model.Province;
import ouc.lm.coolweather.util.HttpCallbackListener;
import ouc.lm.coolweather.util.HttpUtil;
import ouc.lm.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

import ouc.lm.coolweather.R;


/**
 * Created by Lavida on 2016-04-14.
 */
public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList=new ArrayList<String>();

    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;
    //选中的省份
    private Province selectedProvince;
    //选中的城市
    private City selectedCity;
    //当前选中的级别
    private int currentLevel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView=(ListView)findViewById(R.id.list_view);
        titleText=(TextView)findViewById(R.id.title_text);
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        coolWeatherDB=CoolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {   //刚开始currentLevel为空，所以直接查询所有省
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
            }
        });
        queryProvinces();
    }
    //查询所有的省，优先从数据库查询，如果没有查询到，再去服务器上查询
    private void queryProvinces(){
        provinceList=coolWeatherDB.loadProvinces();
        if (provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());  //将省份数据放入dataList
            }
            adapter.notifyDataSetChanged();   //通知适配器，数据已经发生改变了
            listView.setSelection(0);   //给数据设置适当的位置
            titleText.setText("中国");
            currentLevel=LEVEL_PROVINCE;
        }else {
            //数据库为空，从服务器中查询
            queryFromServer(null,"province");
        }
    }
    //查询选中省内所有的市，优先从数据库中查询，如果没有查询到，再去服务器中查询
    private void queryCities(){
        cityList=coolWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel=LEVEL_CITY;
        }else {
            //数据库为空，就去服务器中查询
            queryFromServer(selectedProvince.getPrivinceCode(),"city");
        }
    }
    //查询选中市内所有的县，优先从数据库中查询，如果没有查询到，再去服务器中查询
    private void queryCounties(){
        countyList=coolWeatherDB.loadCounties(selectedCity.getId());
        if (countyList.size()>0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel=LEVEL_COUNTY;
        }else {
            //数据库为空，就去服务器中查询
            queryFromServer(selectedCity.getCityCode(),"county");
        }
    }
    //根据代号和类型，从服务器中查询，省市数据
    private void queryFromServer(final String code,final String type){
        String address;
        if (!TextUtils.isEmpty(code)){
            address= "http://www.weather.com.cn/data/list3/city" + code + ".xml";   //如果能够取得到，证明它已经被选了
        }else {
            address="http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();   //显示进度框
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {   //从服务器中查询数据
            @Override
            public void onFinish(String response) {
                boolean result=false;
                if ("province".equals(type)){
                    result= Utility.handleProvincesResponse(coolWeatherDB,response);  //解析服务器中查询的数据，存到数据库中
                }else if ("city".equals(type)){
                    result=Utility.handleCitiesResponse(coolWeatherDB,response,selectedProvince.getId());
                }else if("county".equals(type)){
                    result=Utility.handleCountiesResponse(coolWeatherDB,response,selectedCity.getId());
                }
                if (result){
                    //通过runOnUiThread()方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();           //关闭进度条
                            if ("province".equals(type)){
                                queryProvinces();    //再从数据库中查询所有数据
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                //通过runOnUiThread()方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    //显示进度对话框
    private void showProgressDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("正在加载....");
            progressDialog.setCanceledOnTouchOutside(false);   //无论是否可以被取消i，点击屏幕，就会取消，现在设置其值为false
        }
        progressDialog.show();
    }
    //关闭进度对话框
    private void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
    //捕获Back按键，根据当前级别来判断，应该返回市列表，省列表还是直接退出

    @Override
    public void onBackPressed() {
        if (currentLevel==LEVEL_COUNTY){
            queryCities();
        }else if (currentLevel==LEVEL_CITY){
            queryProvinces();
        }else {
            finish();   //关闭这个活动
        }
    }
}
