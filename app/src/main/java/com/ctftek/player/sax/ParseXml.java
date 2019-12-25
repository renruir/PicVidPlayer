package com.ctftek.player.sax;

import android.util.Log;

import com.ctftek.player.TestActivity;
import com.ctftek.player.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ParseXml {

    private final static String TAG = ParseXml.class.getName();

    //所有图片播放的名称
    private String[] images;
    //所有视频播放的名称
    private String[] videos;

    private List<VideoInfo> videoInfoList;
    private List<ImageInfo> imageInfoList = new ArrayList<>();
    private List<List<ImageInfo>> imgInfos = new ArrayList<>();

    private int videoRec[] = new int[4];

    private String filePath = Utils.filePath;

    public void parseXml(String path) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(path));
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
                        videoRec[0] = Integer.parseInt(ele.getAttribute("x"));
                        videoRec[1] = Integer.parseInt(ele.getAttribute("y"));
                        videoRec[2] = Integer.parseInt(ele.getAttribute("w"));
                        videoRec[3] = Integer.parseInt(ele.getAttribute("h"));
                        Log.d(TAG, "image rect: x:" + videoRec[0] + "y:" + videoRec[1] + ",w:" + videoRec[2] + ",h:" + videoRec[3]);
                    } else if ("TRACK".equals(name)) {
                        Element ele = (Element) node;
                        videoInfoList = getVideoinfo(ele);
                    }
                }
            }
        }
        NodeList imageList = doc.getElementsByTagName("SUB_WINDOW");
        for (int i = 0; i < imageList.getLength(); i++) {
            Node image = imageList.item(i);
            Element elem = (Element) image;
            String[] sb = null;
            for (Node node = image.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String name = node.getNodeName();
                    Log.d(TAG, "SUB_WINDOW parseXml: " + name);
                    if (name.equals("WINDOW")) {
                        Log.d(TAG, "WINDOW: " + node.getTextContent());
                        sb = new String[Integer.parseInt(node.getTextContent())];
                        for (int j = 0; j < sb.length; j++) {
                            sb[j] = "SUBW" + (j + 1);
                        }
                    } else if (name.equals("LIST")) {
                        Element ele = (Element) node;
                        images = getImageNames(ele);
                    } else {
                        Log.d(TAG, "parseXml: " + sb);
                        if (sb != null) {
                            for (int k = 0; k < sb.length; k++) {
                                Log.d(TAG, "sub window name: " + sb[k]);
                                if (name.equals(sb[k])) {
                                    Element ele = (Element) node;
                                    ImageInfo imageInfo = new ImageInfo();
                                    imageInfo.setRect(getSubRect(ele));
                                    imageInfo.setNames(getSubImageNames(ele));
                                    imageInfoList.add(imageInfo);
                                    imgInfos.add(imageInfoList);
                                }
                            }
                        }
                    }


//                    else if (name.equals("SUBW1")) {
//                        Element ele = (Element) node;
//                        ImageInfo imageInfo = new ImageInfo();
//                        imageInfo.setRect(getSubRect(ele));
//                        imageInfo.setNames(getSubImageNames(ele));
//                        imageInfoList.add(imageInfo);
//                    } else if (name.equals("SUBW2")) {
//                        Element ele = (Element) node;
//                        ImageInfo imageInfo = new ImageInfo();
//                        imageInfo.setRect(getSubRect(ele));
//                        imageInfo.setNames(getSubImageNames(ele));
//                        imageInfoList.add(imageInfo);
//                    }
                }
            }
        }
    }

    private int[] getSubRect(Element ele) {
        NodeList propertyEleList = ele.getElementsByTagName("rect");
        int rect[] = new int[4];
        for (int i = 0; i < propertyEleList.getLength(); i++) {
            Node node = propertyEleList.item(i);
            Element propertyEle = (Element) node;
            rect[0] = Integer.parseInt(propertyEle.getAttribute("x"));
            rect[1] = Integer.parseInt(propertyEle.getAttribute("y"));
            rect[2] = Integer.parseInt(propertyEle.getAttribute("w"));
            rect[3] = Integer.parseInt(propertyEle.getAttribute("h"));
            Log.d(TAG, "image rect: " + rect[0] + "," + rect[1] + "," + rect[2] + "," + rect[3]);
        }
        return rect;
    }

    private List<String> getSubImageNames(Element ele) {
        NodeList propertyEleList = ele.getElementsByTagName("a");
        List<String> imageNames = new ArrayList<>();
        for (int i = 0; i < propertyEleList.getLength(); i++) {
            Node node = propertyEleList.item(i);
            Element propertyEle = (Element) node;
            String p = filePath + File.separator + images[Integer.parseInt(propertyEle.getAttribute("i"))];
            Log.d(TAG, "getSubImageNames: " + p);
            imageNames.add(p);
        }
        return imageNames;
    }

    private String[] getImageNames(Element ele) {
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
                videoInfo.setName(filePath + File.separator + name);
                String vol = propertyEle.getAttribute("vol");
                videoInfo.setVol(Integer.parseInt(vol));
                String interval = propertyEle.getAttribute("interval");
                videoInfo.setInterval(Integer.parseInt(interval));
                videoInfo.setRect(videoRec);
                videoInfos.add(videoInfo);
            }
        }
        return videoInfos;
    }

    public List<List<ImageInfo>> getImagesInfo() {
        return imgInfos;
    }

    public List<VideoInfo> getVideoInfoList() {
        return videoInfoList;
    }


    public class ImageInfo {
        List<String> names;
        int[] rect;

        public List<String> getNames() {
            return names;
        }

        public void setNames(List<String> names) {
            this.names = names;
        }

        public int[] getRect() {
            return rect;
        }

        public void setRect(int[] rect) {
            this.rect = rect;
        }
    }

    public class VideoInfo {
        int[] rect;
        String name;
        int vol;
        int interval;

        public int[] getRect() {
            return rect;
        }

        public void setRect(int[] rect) {
            this.rect = rect;
        }

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
