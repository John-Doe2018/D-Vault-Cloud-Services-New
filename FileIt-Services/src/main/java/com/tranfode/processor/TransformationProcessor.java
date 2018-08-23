package com.tranfode.processor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.tranfode.Constants.BinderConstants;
import com.tranfode.domain.BinderList;
import com.tranfode.domain.Children;
import com.tranfode.domain.FileItContext;
import com.tranfode.util.CloudPropertiesReader;
import com.tranfode.util.CloudStorageConfig;
import com.tranfode.util.FileItException;

public class TransformationProcessor {
	FileItContext fileItContext;
	List<String> pathNamesList = new ArrayList<String>();

	public boolean processHtmlToBinderXml(BinderList binderObject) throws FileItException {
		prepareBinderXML(binderObject);
		return true;
	}

	public void prepareBinderXML(BinderList binderlist) throws FileItException {
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		String bucketName = CloudPropertiesReader.getInstance().getString("bucket.name");
		try {
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			org.w3c.dom.Document document = documentBuilder.newDocument();
			// root element
			Element root = document.createElement("map");
			document.appendChild(root);
			String uniqueID = UUID.randomUUID().toString();
			root.setAttribute("id", uniqueID);

			Element title = document.createElement("title");
			title.setNodeValue(binderlist.getName());
			root.appendChild(title);

			Element body = document.createElement("body");
			root.appendChild(body);
			Element topicref = document.createElement("topicref");
			body.appendChild(topicref);

			// set an attribute to topicref element
			Attr attr = document.createAttribute("navtitle");
			attr.setValue(binderlist.getName());
			Attr type = document.createAttribute("type");
			type.setValue(BinderConstants.BINDER);
			Attr id = document.createAttribute("id");
			id.setValue("topicref");
			Attr classification = document.createAttribute("classification");
			classification.setValue(binderlist.getClassification());
			topicref.setAttributeNode(attr);
			topicref.setAttributeNode(type);
			topicref.setAttributeNode(classification);
			topicref.setAttributeNode(id);
			for (Children child : binderlist.getChildren()) {
				Element topic = document.createElement("topic");
				topic.setAttribute(BinderConstants.NAME, child.getName());
				topic.setAttribute(BinderConstants.PATH, "Images" + "/" + binderlist.getName() + "/" + child.getName());
				topic.setAttribute(BinderConstants.TYPE, child.getType());
				topic.setAttribute(BinderConstants.VERSION, child.getVersion());
				topic.setAttribute(BinderConstants.ID, (String.valueOf(child.getId())));
				topicref.appendChild(topic);
				pathNamesList.add(child.getPath());
			}
			FileItContext.add(BinderConstants.CONTXT_PATH_NAMES, pathNamesList);

			// create the xml file
			// transform the DOM Object to an XML File
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Result res = new StreamResult(baos);
			transformer.transform(domSource, res);
			InputStream isFromFirstData = new ByteArrayInputStream(baos.toByteArray());
			oCloudStorageConfig.uploadFile(bucketName, "files/" + binderlist.getName() + ".xml", isFromFirstData,
					"application/xml");
		} catch (Exception e) {
			throw new FileItException(e.getMessage());
		}

	}

	public BinderList createBinderList(String htmlContent) throws FileItException {
		BinderList binderObject = null;
		ObjectMapper objectMapper = new ObjectMapper();
		JsonFactory f = new JsonFactory();
		JsonParser jp = null;
		try {
			jp = f.createJsonParser(htmlContent);
			jp.nextToken();
			while (jp.nextToken() == JsonToken.FIELD_NAME) {
				binderObject = objectMapper.readValue(jp, BinderList.class);
			}
		} catch (IOException e) {
			throw new FileItException(e.getMessage());
		}
		return binderObject;
	}

}