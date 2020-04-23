package utils;

import entity.Options;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by 周思成 on  2020/3/27 11:18
 */

public class BootYaml {


    public static Options getYaml(String yamlName) throws FileNotFoundException {
        Yaml yaml = new Yaml();// 这个需要的jar为:org.yaml.snakeyaml

        File file = new File(yamlName);
        //MailConfig 这个是这个主函数所在的类的类名
//        InputStream resourceAsStream = BootYaml.class.getClassLoader()
//                .getResourceAsStream(yamlName);

        InputStream resourceAsStream = new FileInputStream(file);
        //加载流,获取yaml文件中的配置数据，然后转换为Map，
//        Map obj = (Map) yaml.load(resourceAsStream);
//        return obj;
        //Options obj = (Options) yaml.load(resourceAsStream);
        return yaml.loadAs(resourceAsStream,Options.class);

    }

    public static void main(String[] args) {
        Yaml yaml = new Yaml();// 这个需要的jar为:org.yaml.snakeyaml

        //MailConfig 这个是这个主函数所在的类的类名
        InputStream resourceAsStream = BootYaml.class.getClassLoader()
                .getResourceAsStream("properties.yml");

        //加载流,获取yaml文件中的配置数据，然后转换为Map，
        //Options obj = (Options) yaml.load(resourceAsStream);
        Options obj =  yaml.loadAs(resourceAsStream,Options.class);
        System.out.println(obj.getOtherNodes()[0].getAddress());

    }
}
