package com.tranfode.processor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.tranfode.Constants.BinderConstants;
import com.tranfode.Constants.CloudFileConstants;
import com.tranfode.domain.BookRequests;
import com.tranfode.util.CloudFilesOperationUtil;

public class AddFileProcessor {
	static CloudFilesOperationUtil cloudFilesOperationUtil = new CloudFilesOperationUtil();
	public void updateXML(String bookName, List<BookRequests> oBookRequests) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		InputStream oInputStream = cloudFilesOperationUtil
				.getFIleInputStream("files/" + bookName + ".xml");
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(oInputStream);
		Node topicref = doc.getElementsByTagName("topicref").item(0);
		for (BookRequests oBookRequests2 : oBookRequests) {
			Element topic = doc.createElement("topic");
			topic.setAttribute(BinderConstants.NAME, oBookRequests2.getName());
			topic.setAttribute(BinderConstants.PATH, "Images" + "/" + bookName + "/" + oBookRequests2.getName());
			topic.setAttribute(BinderConstants.TYPE, oBookRequests2.getType());
			topic.setAttribute(BinderConstants.VERSION, oBookRequests2.getVersion());
			topic.setAttribute(BinderConstants.ID, oBookRequests2.getId());
			topicref.appendChild(topic);
		}
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource domSource = new DOMSource(doc);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Result res = new StreamResult(baos);
		transformer.transform(domSource, res);
		InputStream isFromFirstData = new ByteArrayInputStream(baos.toByteArray());
		cloudFilesOperationUtil.fIleUploaded("files/" + bookName + ".xml", isFromFirstData, CloudFileConstants.XMLFILETYPE);
	}
}
