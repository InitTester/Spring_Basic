package com.fastcampus.ch3.diCopy4;

import com.google.common.reflect.ClassPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component class Car{

    @Resource
    Engine engine;
//    @Resource
    Door door;

    @Override
    public String toString() {
        return "Car{" +
                "engine=" + engine +
                ", door=" + door +
                '}';
    }
}
@Component class SportsCar extends Car{}
@Component class Truck extends Car{}
@Component class Engine{}

@Component class Door{}

class AppContext {
    Map map; // 객체 저장소

    AppContext(){
        map = new HashMap();
        doComponentScan();
        doAutowired();
        doResource();
    }

    private void doResource() {
        // map에 저장된 객체 iv중에 @Resource가 붙어 있으면
        // map에서 iv의 이름에 맞는 객체를 찾아서 연결(객체의 주소를 iv저장)
        try {
            for(Object bean : map.values()){
                for(Field fld : bean.getClass().getDeclaredFields()){
                    if(fld.getAnnotation(Resource.class)!=null) //byName
                        fld.set(bean, getBean(fld.getName())); // car.engine = obj;
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void doAutowired() {
        // map에 저장된 객체 iv중에 @Autowired가 붙어 있으면
        // map에서 iv의 타입에 맞는 객체를 찾아서 연결(객체의 주소를 iv저장)
        try {
            for(Object bean : map.values()){
                for(Field fld : bean.getClass().getDeclaredFields()){
                    if(fld.getAnnotation(Autowired.class)!=null) //byType
                        fld.set(bean, getBean(fld.getType())); // car.engine = obj;
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void doComponentScan() {
        /*
        *   1. 패키지내의 클래스 목록을 가져온다.
        *   2. 반목문으로 클래스를 하나씩 읽어와서 @Component이 붙어 있는지 확인
        *   3. @Component가 붙어있으면 객체를 생성해서 map에 저장
        * */
        try {
            ClassLoader classLoader =  AppContext.class.getClassLoader();
            ClassPath classPath = ClassPath.from(classLoader);

            Set<ClassPath.ClassInfo> set = classPath.getTopLevelClasses("com.fastcampus.ch3.diCopy4");

            for(ClassPath.ClassInfo classInfo : set){
                Class clazz = classInfo.load();
                Component component = (Component)clazz.getAnnotation(Component.class);
                if(component != null) {
                    String id = StringUtils.uncapitalize(classInfo.getSimpleName());
                    map.put(id,clazz.newInstance());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // byName
    Object getBean(String key){return map.get(key); }

    // byType
    Object getBean(Class clazz){
        for(Object obj: map.values()){
            if(clazz.isInstance(obj))
                return obj;
        }
        return null;
    }
}

public class Main4 {
    public static void main(String[] args)  throws Exception  {
        AppContext ac = new AppContext();
        Car car = (Car)ac.getBean("car"); // byName으로 객체를 검색
        Engine engine = (Engine)ac.getBean("engine");
        Door door = (Door) ac.getBean(Door.class);

//        car.engine = engine;
//        car.door = door;
        System.out.println("car = " + car);
        System.out.println("engine = " + engine);
        System.out.println("door = " + door);
    }
}