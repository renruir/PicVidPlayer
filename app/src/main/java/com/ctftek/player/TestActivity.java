package com.ctftek.player;

import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ctftek.player.sax.SaxService;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class TestActivity extends AppCompatActivity {
    private static final String TAG = TestActivity.class.getName();
    Button btn;
    private String[] images;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        btn = (Button) findViewById(R.id.sax_xml);
        //点击按钮，开启线程访问网络
        btn.setOnClickListener(v -> {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    // 设置XML文档的路径
                    String xmlPath = "/mnt/sdcard/mediaResource/playerlist.xml";
                    try {
                        File path = new File(xmlPath);
                        InputStream inputStream = new FileInputStream(path);
                        //调用类SaxService：解析流，同时设定需要解析的节点
                        List<HashMap<String, String>> list = SaxService.readXML(inputStream, "MAIN_WINDOW");
                        Log.d(TAG, "run: " + list.size());
                        for (HashMap<String, String> map : list) {
                            Log.d(TAG, "xml: " + map.toString());
                            Log.d(TAG, "value : " + map.get("rect"));
                        }
//                        parseXmlByPull();
                        parseXml();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        });
    }

    private void parseXml() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File("/mnt/sdcard/mediaResource/playerlist.xml"));
        NodeList dogList = doc.getElementsByTagName("MAIN_WINDOW");
        System.out.println("共有" + dogList.getLength() + "个dog节点");
        for (int i = 0; i < dogList.getLength(); i++) {
            Node dog = dogList.item(i);
            Element elem = (Element) dog;
//            System.out.println("id:" + elem.getAttribute("id"));
            for (Node node = dog.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String name = node.getNodeName();
                    Log.d(TAG, "parseXml: " + name);
                    if (name.equals("rect")) {
                        Element ele = (Element) node;
                        Log.d(TAG, "rect: " + ele.getAttribute("w"));
                    } else if ("TRACK".equals(name)) {
                        Element ele = (Element) node;
                        List<VideoInfo> videoInfos = getVideoinfo(ele);
                    }
                }
            }
        }
        NodeList imageList = doc.getElementsByTagName("SUB_WINDOW");
        System.out.println("共有" + imageList.getLength() + "个image节点");
        for (int i = 0; i < imageList.getLength(); i++) {
            Node image = imageList.item(i);
            Element elem = (Element) image;
            for (Node node = image.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String name = node.getNodeName();
                    Log.d(TAG, "SUB_WINDOW parseXml: " + name);
                    if(name.equals("WINDOW")){
                        Log.d(TAG, "WINDOW: " + node.getTextContent());
                    } else if(name.equals("LIST")){
                        Element ele = (Element) node;
                        images = getImageNames(ele);
                    } else if(name.equals("SUBW1")){
                        Element ele = (Element) node;
                        ImageInfo imageInfo = new ImageInfo();
                        imageInfo.setRect(getSubRect(ele));
                        imageInfo.setNames(getSubImageNames(ele));
                    }
                }
            }
        }
    }

    private int[] getSubRect(Element ele){
        NodeList propertyEleList = ele.getElementsByTagName("rect");
        int rect[] = new int[4];
        for (int i = 0; i < propertyEleList.getLength(); i++) {
            Node node = propertyEleList.item(i);
            Element propertyEle = (Element) node;
            rect[0] = Integer.parseInt(propertyEle.getAttribute("x"));
            rect[1] = Integer.parseInt(propertyEle.getAttribute("y"));
            rect[2] = Integer.parseInt(propertyEle.getAttribute("w"));
            rect[3] = Integer.parseInt(propertyEle.getAttribute("h"));
            Log.d(TAG, "getSub: " + rect[0]+"," +rect[1]+"," +rect[2]+"," +rect[3]);
        }
        return rect;
    }

    private String[] getSubImageNames(Element ele){
        NodeList propertyEleList = ele.getElementsByTagName("a");
        String imageNames[] = new String[propertyEleList.getLength()];
        for (int i = 0; i < propertyEleList.getLength(); i++) {
            Node node = propertyEleList.item(i);
            Element propertyEle = (Element) node;
            imageNames[i] = images[Integer.parseInt(propertyEle.getAttribute("i"))];
            Log.d(TAG, "getSubImageNames: " + imageNames[i]);
        }
        return imageNames;
    }

    private String[] getImageNames(Element ele){
        NodeList propertyEleList = ele.getElementsByTagName("P");
        String[] allImageNames = new String[propertyEleList.getLength()];
        for (int i = 0; i < propertyEleList.getLength(); i++) {
            Node node = propertyEleList.item(i);
            allImageNames[i] = node.getTextContent();
        }
        return allImageNames;
    }

    private List<VideoInfo> getVideoinfo(Element ele) {

        NodeList propertyEleList = ele.getElementsByTagName("a");//根据标签名称获取标签元素列表
        Log.d(TAG, "getVideoinfo: " + propertyEleList.getLength());
        List<VideoInfo> videoInfos = new ArrayList<>();
        for (int i = 0; i < propertyEleList.getLength(); i++) {
            Node node = propertyEleList.item(i);
            if (node instanceof Element) {
                VideoInfo videoInfo = new VideoInfo();
                Element propertyEle = (Element) node;
                String name = propertyEle.getAttribute("name");
                System.out.println("propertyEle: name == " + name);
                videoInfo.setName(name);
                String vol = propertyEle.getAttribute("vol");
                System.out.println("propertyEle: value == " + vol);
                videoInfo.setVol(Integer.parseInt(vol));
                String interval = propertyEle.getAttribute("interval");
                System.out.println("propertyEle: value == " + interval);
                videoInfo.setInterval(Integer.parseInt(interval));
                videoInfos.add(videoInfo);
            }
        }
        return videoInfos;
    }


    class ImageInfo {
        String[] names;
        int[] rect;

        public String[] getNames() {
            return names;
        }

        public void setNames(String[] names) {
            this.names = names;
        }

        public int[] getRect() {
            return rect;
        }

        public void setRect(int[] rect) {
            this.rect = rect;
        }
    }

    class VideoInfo {
        String name;
        int vol;
        int interval;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getVol() {
            return vol;
        }

        public void setVol(int vol) {
            this.vol = vol;
        }

        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }
    }
}
