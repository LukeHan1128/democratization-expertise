package com.hamonize.cmmn.util;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import com.hamonize.cmmn.CmmnMap;



/**
 * <pre>
 * @패키지명   : com.hamonize.cmmn.util
 * @파일명     : CmmExcelService.java
 * </pre>
 * 
 * 설명 : Excel 공통 서비스
 * 
 * @회사     : 인베슘
 * @작성자   : 김승원
 * @작성일자 : 2020. 05. 21.
 */
@SuppressWarnings("deprecation")
public class CmmnExcelService extends AbstractExcelView{

	@SuppressWarnings("unchecked")
	protected void buildExcelDocument(Map<String, Object> model,
			org.apache.poi.hssf.usermodel.HSSFWorkbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		

		CmmnMap data = (CmmnMap) model.get("data");
		 
		String userAgent = request.getHeader("User-Agent");
		//String fileName = data.getString("excelName")+".xls";
		String fileName = new StringBuilder(data.getString("excelName")+"_").append(DateUtil.getToday("yyyyMMddHHmmss")).append(".xlsx").toString();

		if (userAgent.indexOf("MSIE") > -1 || userAgent.contains("Trident")) {
			fileName = URLEncoder.encode(fileName, "utf-8");
		} else {
			fileName = new String(fileName.getBytes("utf-8"), "iso-8859-1");
		}
		response.setHeader("Content-Disposition", "attachment; filename=\""	+ fileName + "\";");
		response.setHeader("Content-Transfer-Encoding", "binary");

		//create a new workbook
		SXSSFWorkbook wb = new SXSSFWorkbook(100);		
		//create a new Excel sheet
		Sheet sheet = wb.createSheet(data.getString("excelName")+" 내역");

		Row header = sheet.createRow(0);
        String[] headerName=(String[])data.get("header");
        for(int i=0,max=headerName.length;i<max;i++){
        	header.createCell(i).setCellValue(headerName[i]);
        }
        
        // create data rows
        int rowCount = 1;
		List<CmmnMap> list = (List<CmmnMap>)data.get("list");
		int maxColumnWidth = 255*256; // The maximum column width 
		
        for(int i=0,max=list.size();i<max;i++){
        	Row aRow = sheet.createRow(rowCount++);
        	
        	for(int ii=0,max2=headerName.length;ii<max2;ii++){

        		aRow.createCell(ii).setCellValue(list.get(i).getValue(ii).toString() );
        	
        		int width = (sheet.getColumnWidth(ii));
        		if (width > maxColumnWidth) { 
        			width = maxColumnWidth;  
				}
        	}
        }
        
        	//write the excel to a file
        	/*
        	ByteArrayOutputStream  fileOut = new ByteArrayOutputStream();
            wb.write(fileOut);
            InputStream filein = new ByteArrayInputStream(fileOut.toByteArray());
            fileOut.close();
            
          //Add password protection and encrypt the file
            
            POIFSFileSystem fs = new POIFSFileSystem();
            EncryptionInfo info = new EncryptionInfo(fs, EncryptionMode.agile);
            Encryptor enc = info.getEncryptor();
            enc.confirmPassword("qqq");
            
            OPCPackage opc = OPCPackage.open(filein);
    		OutputStream os = enc.getDataStream(fs);
    		opc.save(os);
    		opc.close();
    		System.out.println("File created!!");
    		*/
        
    		OutputStream fileOut2 = response.getOutputStream(); 
    		wb.write(fileOut2);
    	 	fileOut2.close();


	}
}
