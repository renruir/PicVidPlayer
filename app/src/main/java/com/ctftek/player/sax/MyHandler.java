package com.ctftek.player.sax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


//类：MyHandler，继承DefaultHandler类，用于解析XML数据。
//之后在MainActivity中通过设定具体的nodeName来实例化MyHandler
public class MyHandler extends DefaultHandler {
    private List<HashMap<String, String>> list = null; //解析后的XML内容
    private HashMap<String, String> map = null;  //存放当前需要记录的节点的XML内容
    private String currentTag = null;//当前读取的XML节点
    private String currentValue = null;//当前节点的XML文本值
    private String nodeName = null;//需要解析的节点名称

    public MyHandler(String nodeName) {
        // 设置需要解析的节点名称
        this.nodeName = nodeName;
    }

    @Override
    public void startDocument() throws SAXException {
        // 接收文档开始的通知
        // 实例化ArrayList用于存放解析XML后的数据
        list = new ArrayList<HashMap<String, String>>();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        // 接收元素开始的通知
        if (qName.equals(nodeName)) {
            //如果当前运行的节点名称与设定需要读取的节点名称相同，则实例化HashMap
            map = new HashMap<String, String>();
        }
        //Attributes为当前节点的属性值，如果存在属性值，则属性值也读取。
        if (attributes != null && map != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                //读取到的属性值，插入到Map中。
                map.put(attributes.getQName(i), attributes.getValue(i));
            }
        }
        //记录当前节点的名称。
        currentTag = qName;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        // 接收元素中字符数据的通知。
        //当前节点有值的情况下才继续执行
        if (currentTag != null && map != null) {
            //获取当前节点的文本值，ch这个直接数组就是存放的文本值。
            currentValue = new String(ch, start, length);
            if (currentValue != null && !currentValue.equals("")
                    && !currentValue.equals("\n")) {
                //读取的文本需要判断不能为null、不能等于”“、不能等于”\n“
                map.put(currentTag, currentValue);
            }
        }
        //读取完成后，需要清空当前节点的标签值和所包含的文本值。
        currentTag = null;
        currentValue = null;
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        // 接收元素结束的通知。
        if (qName.equals(nodeName)) {
            //如果读取的结合节点是我们需要关注的节点，则把map加入到list中保存
            list.add(map);
            //使用之后清空map，开始新一轮的读取person。
            map = null;
        }
    }

    //方法：获取解析之后的数据
    public List<HashMap<String, String>> getList() {
        return list;
    }
}