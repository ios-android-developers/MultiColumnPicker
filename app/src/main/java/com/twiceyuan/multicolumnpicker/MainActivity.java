package com.twiceyuan.multicolumnpicker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.litesuits.orm.db.assit.QueryBuilder;
import com.twiceyuan.library.MultiColumnPicker;
import com.twiceyuan.multicolumnpicker.adapter.CustomLeftAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDatabase();
        initBusinessDatabase();
    }

    public void test(View view) {
        MultiColumnPicker<City, City> picker = new MultiColumnPicker<>(this);
        picker.setLeftContent(getProvince());
        picker.setOnLeftSelected((position, city) -> getCity(city));
        picker.setOnRightSelected((position, city) -> action(city));
        picker.setMapLeftString(city -> city.name);
        picker.setMapRightString(city -> city.fullName);
        picker.setMapLeftId(city -> city.id);
        picker.setMapRightId(city -> city.id);
        picker.setLeftDefault("江苏");
        picker.show();
    }

    private List<City> getProvince() {
        return App.db.query(
                QueryBuilder.create(City.class).whereEquals("parent", "86").orderBy("id"));
    }

    private List<City> getCity(City city) {
        return App.db.query(QueryBuilder.create(City.class).whereEquals("parent", city.id));
    }


    public void test2(View view) {
        MultiColumnPicker<Business, Business> picker = new MultiColumnPicker<>(this);
        picker.setLeftContent(getCategories());
        picker.setOnLeftSelected((position, business) -> getBusiness(business));
        picker.setOnRightSelected((position, business) -> action2(business));
        picker.setMapLeftString(business -> business.name);
        picker.setMapRightString(business -> business.name);
        picker.setMapLeftId(business -> business.id);
        picker.setMapRightId(business -> business.id);
        picker.setLeftAdapter((mapper, businesses) ->
                new CustomLeftAdapter<>(businesses, mapper)); // 配置自定义适配器
        picker.setLeftDefault(0);
        picker.show();
    }

    private List<Business> getCategories() {
        return App.db.query(QueryBuilder.create(Business.class).whereEquals("parent", "00"));
    }

    private List<Business> getBusiness(Business father) {
        return App.db.query(QueryBuilder.create(Business.class).whereEquals("parent", father.id));
    }

    private void action(City city) {
        Toast.makeText(this, city.fullName + city.id, Toast.LENGTH_SHORT).show();
    }

    private void action2(Business business) {
        Toast.makeText(this, business.id + "/" + business.name, Toast.LENGTH_SHORT).show();
    }

    /**
     * 初始化城市数据库（第一次）
     */
    public void initDatabase() {
        if (App.db.queryCount(City.class) > 0) {
            return;
        }
        InputStream is = getResources().openRawResource(R.raw.city);
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader reader2 = new BufferedReader(reader);
        String temp;
        try {
            while ((temp = reader2.readLine()) != null) {
                String args[] = temp.split("\\$");
                App.db.insert(new City(args[0], args[3], args[1], args[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化行业信息数据（第一次）
     */
    public void initBusinessDatabase() {
        if (App.db.queryCount(Business.class) > 0) {
            return;
        }
        InputStream is = getResources().openRawResource(R.raw.business);
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader reader2 = new BufferedReader(reader);
        String temp;
        try {
            while ((temp = reader2.readLine()) != null) {
                String args[] = temp.split("\\$");
                App.db.insert(new Business(args[0], args[1], args[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
