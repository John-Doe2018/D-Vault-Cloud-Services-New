package com.tranfode.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.tranfode.Constants.BinderConstants;
import com.tranfode.Constants.CloudFileConstants;
import com.tranfode.domain.AddClassificationRequest;
import com.tranfode.domain.AddClassificationResponse;
import com.tranfode.domain.AddFileRequest;
import com.tranfode.domain.BinderList;
import com.tranfode.domain.BookMarkDetails;
import com.tranfode.domain.BookMarkRequest;
import com.tranfode.domain.BookMarkResponse;
import com.tranfode.domain.CreateBinderRequest;
import com.tranfode.domain.CreateBinderResponse;
import com.tranfode.domain.DeleteBookRequest;
import com.tranfode.domain.DeleteFileRequest;
import com.tranfode.domain.DownloadFileRequest;
import com.tranfode.domain.FileItContext;
import com.tranfode.domain.GetBookTreeRequest;
import com.tranfode.domain.GetImageRequest;
import com.tranfode.domain.SearchBookRequest;
import com.tranfode.domain.SearchBookResponse;
import com.tranfode.processor.AddClassificationProcessor;
import com.tranfode.processor.AddFileProcessor;
import com.tranfode.processor.BookTreeProcessor;
import com.tranfode.processor.ContentProcessor;
import com.tranfode.processor.DeleteBookProcessor;
import com.tranfode.processor.LookupBookProcessor;
import com.tranfode.processor.PrepareClassificationMap;
import com.tranfode.processor.TransformationProcessor;
import com.tranfode.processor.UpdateMasterJson;
import com.tranfode.util.BookMarkUtil;
import com.tranfode.util.CloudPropertiesReader;
import com.tranfode.util.CloudStorageConfig;
import com.tranfode.util.FileInfoPropertyReader;
import com.tranfode.util.FileItException;
import com.tranfode.util.FileUtil;

public class BinderService {

	/**
	 * @param createBinderRequest
	 * @return
	 * @throws FileItException
	 */
	@POST
	@Path("create")
	public CreateBinderResponse createBinder(CreateBinderRequest createBinderRequest) throws FileItException {
		CreateBinderResponse createBinderResponse = new CreateBinderResponse();
		try {
			FileUtil.checkTestJson();

			String htmlContent = createBinderRequest.getHtmlContent();
			TransformationProcessor transformationProcessor = new TransformationProcessor();
			BinderList listOfBinderObj = transformationProcessor.createBinderList(htmlContent);
			transformationProcessor.processHtmlToBinderXml(listOfBinderObj);
			UpdateMasterJson updateMasterJson = new UpdateMasterJson();
			updateMasterJson.prepareMasterJson(listOfBinderObj);
			createBinderResponse.setSuccessMsg("Binder Successfully Created.");
			JSONObject parentObj = new JSONObject();
			InputStream is = new ByteArrayInputStream(parentObj.toJSONString().getBytes());
			CloudStorageConfig.getInstance().uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					CloudFileConstants.BOOKLISTJSON, is, CloudFileConstants.JSONFILETYPE);
			PrepareClassificationMap
					.createClassifiedMap(FileInfoPropertyReader.getInstance().getString("masterjson.file.path"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return createBinderResponse;
	}

	/**
	 * @param oGetImageRequest
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("getImage")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<String> getFile(GetImageRequest oGetImageRequest) throws Exception {
		InputStream fis;
		FileItContext oFileItContext = new FileItContext();
		List<String> oImages = new ArrayList<>();
		JSONObject detailsObj = new JSONObject();
		JSONObject pagedetaillsObj = new JSONObject();
		detailsObj.put("pageCount", 0);
		detailsObj.put("imageMapList", oImages);
		String wordToSearchFor1 = oGetImageRequest.getClassification() + '/' + oGetImageRequest.getBookName() + '/'
				+ "Contents/";
		List<String> oList = CloudStorageConfig.getInstance()
				.listBucket(CloudPropertiesReader.getInstance().getString("bucket.name"));
		Collection<String> filtered = Collections2.filter(oList, Predicates.containsPattern(wordToSearchFor1));
		List<String> booklist = new ArrayList<>();
		if (null != oFileItContext.get(oGetImageRequest.getBookName())) {
			pagedetaillsObj = (JSONObject) oFileItContext.get(oGetImageRequest.getBookName());
		} else {
			pagedetaillsObj = ContentProcessor.getInstance().getBookPageInfo(filtered, oGetImageRequest.getBookName());
		}
		for (String docname : filtered) {
			if (Integer.valueOf(pagedetaillsObj.get(docname).toString()) >= oGetImageRequest.getRangeList().get(0)
					&& Integer.valueOf(pagedetaillsObj.get(docname).toString()) >= oGetImageRequest.getRangeList()
							.get(1)) {
				booklist.add(docname);
				break;
			} else if (Integer.valueOf(pagedetaillsObj.get(docname).toString()) >= oGetImageRequest.getRangeList()
					.get(0)
					&& Integer.valueOf(pagedetaillsObj.get(docname).toString()) < oGetImageRequest.getRangeList()
							.get(1)) {
				booklist.add(docname);
			} else if(Integer.valueOf(pagedetaillsObj.get(docname).toString()) > oGetImageRequest.getRangeList()
					.get(0)
					&& Integer.valueOf(pagedetaillsObj.get(docname).toString()) > oGetImageRequest.getRangeList()
							.get(1)){
				booklist.add(docname);
				break;
			}
		}

		if (booklist.size() == 1) {
			String extension = FilenameUtils.getExtension(booklist.get(0));
			String fileName = FilenameUtils.getName(booklist.get(0));
			fis = CloudStorageConfig.getInstance().getFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					booklist.get(0));
			detailsObj = ContentProcessor.getInstance().processContentImage(oGetImageRequest.getBookName(), fis,
					oGetImageRequest.getClassification() + "/" + oGetImageRequest.getBookName() + "/Images/", extension,
					fileName, Integer.valueOf(detailsObj.get("pageCount").toString()),
					(List<String>) detailsObj.get("imageMapList"), oGetImageRequest.getRangeList().get(0),
					oGetImageRequest.getRangeList().get(1));
		} else {
			for (int k = 0; k < booklist.size(); k++) {
				String extension = FilenameUtils.getExtension(booklist.get(k));
				String fileName = FilenameUtils.getName(booklist.get(k));
				fis = CloudStorageConfig.getInstance()
						.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"), booklist.get(k));
				detailsObj = ContentProcessor.getInstance().processContentImage(oGetImageRequest.getBookName(), fis,
						oGetImageRequest.getClassification() + "/" + oGetImageRequest.getBookName() + "/Images/",
						extension, fileName, Integer.valueOf(detailsObj.get("pageCount").toString()),
						(List<String>) detailsObj.get("imageMapList"), oGetImageRequest.getRangeList().get(k), null);
			}
		}

		return (List<String>) detailsObj.get("imageMapList");
	}

	/**
	 * @param oDownloadFileRequest
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("download")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONObject downloadFile(DownloadFileRequest oDownloadFileRequest) throws Exception {
		ContentProcessor oContentProcessor = new ContentProcessor();
		String url = null;
		if (null != oDownloadFileRequest.getFileName()) {
			if (oDownloadFileRequest.getFileName().size() > 1) {
				File oFile = new File(this.getClass().getClassLoader().getResource("/").getPath()
						+ oDownloadFileRequest.getBookName() + ".zip");
				oContentProcessor.getMultipleFileDownload(oDownloadFileRequest.getClassificationname(),
						oDownloadFileRequest.getBookName(), oDownloadFileRequest.getFileName(), oFile);
				InputStream fis = new FileInputStream(oFile);
				CloudStorageConfig.getInstance()
						.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
								"/" + oDownloadFileRequest.getClassificationname() + "/"
										+ oDownloadFileRequest.getBookName() + "/" + oDownloadFileRequest.getBookName()
										+ ".zip",
								fis, "application/zip");
				url = CloudStorageConfig.getInstance()
						.getSignedString(CloudPropertiesReader.getInstance().getString("bucket.name"),
								"/" + oDownloadFileRequest.getClassificationname() + "/"
										+ oDownloadFileRequest.getBookName() + "/" + oDownloadFileRequest.getBookName()
										+ ".zip");
			} else {
				url = CloudStorageConfig.getInstance().getSignedString(
						CloudPropertiesReader.getInstance().getString("bucket.name"),
						oDownloadFileRequest.getClassificationname() + "/" + oDownloadFileRequest.getBookName()
								+ "/Contents/" + oDownloadFileRequest.getFileName().get(0));
			}
		} else {
			File oFile = new File(this.getClass().getClassLoader().getResource("/").getPath()
					+ oDownloadFileRequest.getBookName() + ".zip");
			oContentProcessor.getZipFile(oDownloadFileRequest.getClassificationname(),
					oDownloadFileRequest.getBookName(), oFile);
			InputStream fis = new FileInputStream(oFile);
			CloudStorageConfig.getInstance().uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"/" + oDownloadFileRequest.getClassificationname() + "/" + oDownloadFileRequest.getBookName() + "/"
							+ oDownloadFileRequest.getBookName() + ".zip",
					fis, "application/zip");
			url = CloudStorageConfig.getInstance()
					.getSignedString(CloudPropertiesReader.getInstance().getString("bucket.name"),
							"/" + oDownloadFileRequest.getClassificationname() + "/"
									+ oDownloadFileRequest.getBookName() + "/" + oDownloadFileRequest.getBookName()
									+ ".zip");
		}
		JSONObject object = new JSONObject();
		object.put("URL", url);
		return object;
	}

	/**
	 * @param multipart
	 * @return
	 * @throws Exception
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("imageConvert")
	public Response submit(MultipartBody multipart) throws Exception {
		JSONObject oJsonObject;
		try {
			Attachment file = multipart.getAttachment("file");
			Attachment file1 = multipart.getAttachment("bookName");
			Attachment file2 = multipart.getAttachment("path");
			Attachment file3 = multipart.getAttachment("type");
			Attachment file4 = multipart.getAttachment("filename");
			Attachment classificationName = multipart.getAttachment("classification");
			String bookName = file1.getObject(String.class);
			String path = file2.getObject(String.class);
			String type = file3.getObject(String.class);
			String fileName = file4.getObject(String.class);
			String className = classificationName.getObject(String.class);
			InputStream fileStream = file.getObject(InputStream.class);
			List<String> oList = CloudStorageConfig.getInstance()
					.listBucket(CloudPropertiesReader.getInstance().getString("bucket.name"));
			Collection<String> filteredPath = Collections2.filter(oList,
					Predicates.containsPattern(classificationName + "/" + bookName + "/Images/"));
			if (filteredPath != null && !filteredPath.isEmpty()) {
				CloudStorageConfig.getInstance().deleteFile(
						CloudPropertiesReader.getInstance().getString("bucket.name"),
						classificationName + "/" + bookName + "/Images/");
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			org.apache.commons.io.IOUtils.copy(fileStream, baos);
			byte[] bytes = baos.toByteArray();
			ContentProcessor contentProcessor = ContentProcessor.getInstance();
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			oJsonObject = contentProcessor.processContent(bookName, className, bais, path, type, fileName);
		} catch (Exception ex) {
			return Response.status(600).entity(ex.getMessage()).build();
		}
		return Response.status(200).entity(oJsonObject).build();
	}

	/**
	 * @param deleteBookRequest
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("delete")
	@Produces("application/json")
	public JSONObject deleteBinder(DeleteBookRequest deleteBookRequest) throws Exception {
		String bookName = deleteBookRequest.getBookName();
		String classificationName = deleteBookRequest.getClassificationName();
		DeleteBookProcessor deleteBookProcessor = new DeleteBookProcessor();
		JSONObject succssMsg = deleteBookProcessor.deleteBookProcessor(bookName, classificationName);
		return succssMsg;
	}

	/**
	 * @param oGetBookTreeRequest
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("getBookTreeDetail")
	@Produces("application/json")
	public JSONObject BookTreeDetail(GetBookTreeRequest oGetBookTreeRequest) throws Exception {
		JSONObject document = BookTreeProcessor.getInstance().processBookXmltoDoc(oGetBookTreeRequest.getBookname(),
				oGetBookTreeRequest.getClassificationname());
		return document;
	}

	/**
	 * @param pathName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 */
	@POST
	@Path("getPDF")
	@Produces("application/pdf")
	public Response getPDF(String pathName) throws FileNotFoundException, IOException, ParseException {
		pathName = FileUtil.correctFilePath(pathName);
		File file = new File(pathName.substring(1, pathName.length() - 1));
		ResponseBuilder response = Response.ok((Object) file);
		response.header("Content-Disposition", "attachment; filename=PrivacyByDesignVer1.0.pdf");
		return response.build();
	}

	/**
	 * @param searchBookRequest
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("searchBook")
	public SearchBookResponse searchBook(SearchBookRequest searchBookRequest) throws Exception {
		SearchBookResponse bookResponse = new SearchBookResponse();
		String bookName = searchBookRequest.getBookName();
		JSONObject jsonObject = null;
		jsonObject = LookupBookProcessor.getInstance().lookupBookbyName(bookName);
		bookResponse.setJsonObject(jsonObject);
		return bookResponse;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("advancedSearch")
	public JSONArray advancedSearch() throws Exception {
		// JSONObject array = null;
		JSONArray jsonArray = null;
		/*
		 * if (FileItContext.get(BinderConstants.CLASSIFIED_BOOK_LIST) != null) {
		 * jsonArray = (JSONArray)
		 * FileItContext.get(BinderConstants.CLASSIFIED_BOOK_LIST); } else { InputStream
		 * oInputStream = CloudStorageConfig.getInstance().getFile(
		 * CloudPropertiesReader.getInstance().getString("bucket.name"),
		 * CloudFileConstants.BOOKLISTJSON); JSONParser parser = new JSONParser(); array
		 * = (JSONObject) parser.parse(new InputStreamReader(oInputStream)); jsonArray =
		 * (JSONArray) array.get("Books"); FileItContext forBookClassifcation = new
		 * FileItContext();
		 * forBookClassifcation.add(BinderConstants.CLASSIFIED_BOOK_LIST, jsonArray); }
		 */

		return jsonArray;

	}

	/**
	 * @param oAddFileRequest
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("addFile")
	public JSONObject addFiles(AddFileRequest oAddFileRequest) throws Exception {
		AddFileProcessor oAddFileProcessor = new AddFileProcessor();
		oAddFileProcessor.updateXML(oAddFileRequest.getBookName(), oAddFileRequest.getClassifcationName(),
				oAddFileRequest.getoBookRequests());
		JSONObject oJsonObject = new JSONObject();
		oJsonObject.put("Success", "File Added Successfully");
		return oJsonObject;
	}

	/**
	 * @param oDeleteFileRequest
	 * @return
	 * @throws FileItException
	 */
	@POST
	@Path("deleteFile")
	public JSONObject deleteFile(DeleteFileRequest oDeleteFileRequest) throws FileItException {
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig().getInstance();
		JSONObject oJsonObject = new JSONObject();
		Element topicElement = null;
		InputStream oInputStream1;
		String path = oDeleteFileRequest.getClassificationName() + "/" + oDeleteFileRequest.getBookName() + "/Contents/"
				+ oDeleteFileRequest.getFileName();
		oCloudStorageConfig.deleteFile(CloudPropertiesReader.getInstance().getString("bucket.name"), path);
		if (oDeleteFileRequest.isBookcreated()) {
			oInputStream1 = oCloudStorageConfig.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"files/" + oDeleteFileRequest.getClassificationName() + "/" + oDeleteFileRequest.getBookName()
							+ ".xml");
			DocumentBuilderFactory docbf = DocumentBuilderFactory.newInstance();
			docbf.setNamespaceAware(true);

			try {
				DocumentBuilder docbuilder = docbf.newDocumentBuilder();
				Document document = docbuilder.parse(oInputStream1);
				NodeList fileList = document.getElementsByTagName("topic");
				for (int i = 0; i < fileList.getLength(); i++) {
					Node element = fileList.item(i);
					if (element.getNodeType() == Node.ELEMENT_NODE) {
						topicElement = (Element) element;
						if (oDeleteFileRequest.getFileName().equals(topicElement.getAttribute("name"))) {
							element.getParentNode().removeChild(topicElement);
							break;
						}
					}
				}
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource domSource = new DOMSource(document);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				Result res = new StreamResult(baos);
				transformer.transform(domSource, res);
				InputStream isFromFirstData = new ByteArrayInputStream(baos.toByteArray());
				oCloudStorageConfig
						.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
								"files/" + oDeleteFileRequest.getClassificationName() + "/"
										+ oDeleteFileRequest.getBookName() + ".xml",
								isFromFirstData, "application/xml");
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		oJsonObject.put("Success", "Deleted Successfully");

		return oJsonObject;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("classifiedData")
	public JSONObject getBookClassification() throws Exception {
		return (JSONObject) FileItContext.get(BinderConstants.CLASSIFIED_BOOK_NAMES);

	}

	/**
	 * @param addClassificationRequest
	 * @return
	 * @throws FileItException
	 */
	@POST
	@Path("addClassification")
	public AddClassificationResponse addBookClassification(AddClassificationRequest addClassificationRequest)
			throws FileItException {
		AddClassificationResponse addClassificationResponse = new AddClassificationResponse();
		String className = addClassificationRequest.getClassificationName();
		try {
			addClassificationResponse = AddClassificationProcessor.getInstance().addClassification(className,
					addClassificationResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return addClassificationResponse;
	}

	/**
	 * @return
	 * @throws FileItException
	 */
	@GET
	@Path("getClassification")
	public List<String> getClassifications() throws FileItException {
		List<String> getClassifications = new ArrayList<String>();
		try {
			getClassifications = AddClassificationProcessor.getInstance().getClassifications();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return getClassifications;
	}

	/**
	 * @param bookMarkRequest
	 * @return
	 * @throws FileItException
	 */
	@POST
	@Path("tagBook")
	public BookMarkResponse bookMark(BookMarkRequest bookMarkRequest) throws FileItException {
		BookMarkResponse bookMarkResponse = new BookMarkResponse();
		String loggedInUser = bookMarkRequest.getUserName();
		String requestedBookName = bookMarkRequest.getBookName();
		String classificationName = bookMarkRequest.getClassificationName();
		BookMarkDetails bookMarkdetails = new BookMarkDetails();
		bookMarkdetails = BookMarkUtil.getInstance().saveUserBookMarkDetails(loggedInUser, requestedBookName,
				classificationName);
		bookMarkResponse.setBookmarkDetails(bookMarkdetails);
		return bookMarkResponse;

	}

	/**
	 * @param bookMarkRequest
	 * @return
	 * @throws FileItException
	 */
	@POST
	@Path("getBookMarks")
	public BookMarkResponse getBookMarks(BookMarkRequest bookMarkRequest) throws FileItException {
		BookMarkResponse bookMarkResponse = new BookMarkResponse();
		String loggedInUser = bookMarkRequest.getUserName();
		List<BookMarkDetails> bookMarkdetails = new ArrayList<BookMarkDetails>();
		bookMarkdetails = BookMarkUtil.getInstance().getBookMarks(loggedInUser);
		if (null != bookMarkdetails) {
			bookMarkResponse.setBookmarkDetailsList(bookMarkdetails);
		} else {
			bookMarkResponse.setErrorMessage("No Data Found");
		}
		return bookMarkResponse;

	}
}
