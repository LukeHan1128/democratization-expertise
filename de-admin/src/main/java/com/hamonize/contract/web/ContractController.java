package com.hamonize.contract.web;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.servlet.ModelAndView;

import com.hamonize.cmmn.CmmnMap;
import com.hamonize.cmmn.service.CmmnService;
import com.hamonize.contract.service.ContractService;
import com.hamonize.contract.service.ContractVO;
import com.hamonize.file.FileUtil;
import com.hamonize.file.service.FileService;
import com.hamonize.file.service.FileVO;
import com.hamonize.login.service.SecurityMember;

import egovframework.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;


@Controller
@RequestMapping("/contract")
public class ContractController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// @Resource(name = "sampleService")
	@Autowired
	private ContractService contractservice;

	@Autowired
	private CmmnService cmmnService;
	
	@Autowired
	private FileUtil fileUtil;
	
	@Autowired
	private FileService fileService;

	@Autowired
	private MessageSource messageSource;

	/**
	 * 글 목록을 조회한다. (pageing)
	 * @param SampleVO - 조회할 정보가 담긴 SampleVO
	 * @param model
	 * @return "SampleList"
	 * @exception Exception
	 */

	@RequestMapping("/list.ivs")
	public String contractList(@RequestParam Map<String, String> params, HttpServletRequest request, ContractVO vo,
			ModelMap model, @AuthenticationPrincipal SecurityMember user) throws Exception {
		CmmnMap param = new CmmnMap();
		param.putAll(params);

		logger.info("----------param-----------------------");
		logger.debug("");
		logger.debug(param.toString());
		logger.debug("");
		logger.debug("----------param-----------------------");

		PaginationInfo paginationInfo = new PaginationInfo();
		paginationInfo.setCurrentPageNo(param.getInt("pageNo") > 0 ? param.getInt("pageNo") : 1); // 현재 페이지 번호
		paginationInfo.setRecordCountPerPage(5); // 한 페이지에 게시되는 게시물 건수
		paginationInfo.setPageSize(5); // 페이징 리스트의 사이즈

		int firstRecordIndex = paginationInfo.getFirstRecordIndex();
		int recordCountPerPage = paginationInfo.getRecordCountPerPage();
		vo.setFirstRecordIndex(firstRecordIndex);
		vo.setRecordCountPerPage(recordCountPerPage);

		//사용자 세션의 회사코드 
		vo.setBizcd(user.getBizcd());
		
		List<ContractVO> list = contractservice.getList(vo);
		int listCount = contractservice.getListCount(vo);
		paginationInfo.setTotalRecordCount(listCount); // 전체 게시물 건 수
		model.addAttribute("userSession", user);
		model.addAttribute("list", list);
		model.addAttribute("paginationInfo", paginationInfo);

		return "contract/list";
	}


	/**
	 * 글을 등록한다.
	 * @param SampleVO - 등록할 정보가 담긴 SampleVO
	 * @param model
	 * @return "sampleSave"
	 * @exception Exception
	 */

	@RequestMapping("/save.ivs")
	public String saleplanSave(HttpServletRequest request, ContractVO vo, ModelMap model
			,@AuthenticationPrincipal SecurityMember user) throws Exception {
		model.addAttribute("userSession", user);
		CmmnMap param = new CmmnMap();

		if (vo.getSeq() != null) {
			vo = contractservice.getView(vo);
			param.put("seq", vo.getSeq());
			param.put("section","contract");
			model.addAttribute("result", vo);
			model.addAttribute("file", cmmnService.selectObject("viewFile", param));
		}else {
			//계약서 등록여부 확인
			//CmmnMap param = new CmmnMap();
			param.put("business_code", vo.getBusiness_code());
			int result = cmmnService.selectCount("getbusiness_code_cnt", param);
			if(result > 0) {
				model.addAttribute("message", "이미 등록된 계약 입니다. 계약서 관리에서 수정해 주세요.");
				model.addAttribute("url", "/contract/list.ivs");
				return "cmmn/message";
			}
			//판매계획서 후 계약서 작성시
			param.put("product_section", vo.getProduct_section());
			param.put("bizcd", vo.getBizcd());
			Map<String, Object> data = new HashMap<String, Object>();
			data.putAll(cmmnService.selectObject("getCompany", param));
			data.putAll(cmmnService.selectObject("getProductSection", param));

			model.addAttribute("getInfo", data);
			model.addAttribute("params", vo);
		}
		return "contract/save";
	}


	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public void upload(MultipartRequest request) {
		MultipartFile file1 = request.getFile("file1");
		System.out.println(file1.getName());
		System.out.println(file1.getOriginalFilename());
		System.out.println(file1.getContentType());
		System.out.println(file1.getSize());
		try {
			File filePath = new File("/tmp");
			if (!filePath.exists()) {
				// 폴더 없는 경우 생성한다
				filePath.mkdir();
			}
			file1.transferTo(new File("/home/ryan/test_upload/" + file1.getOriginalFilename()));
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 글을 등록한다.
	 * @param SampleVO - 등록할 정보가 담긴 SampleVO
	 * @param model
	 * @return "sampleSave"
	 * @exception Exception
	 */
	@RequestMapping("/save.proc")
	public String save(HttpServletRequest request, ContractVO vo, ModelMap model, @RequestParam Map<String, Object> params) throws Exception {
		//seq 생성
		vo.setSeq(contractservice.selectSEQ());
		//첨부파일 관련
		params.put("seq",vo.getSeq() );
		params.put("section","contract");
		
		List<MultipartFile> uploadFileList = new ArrayList<MultipartFile>();
		Map<String, List<MultipartFile>> uploadFile = new HashMap<String, List<MultipartFile>>();
		List<FileVO> fileList = null;
		
		MultipartRequest multipartReq = (MultipartRequest) request;
		Iterator<String> filenames = multipartReq.getFileNames();
		while (filenames.hasNext()) {
			String key = (String) filenames.next();
			uploadFile.put(key, multipartReq.getFiles(key));
			uploadFileList.addAll(multipartReq.getFiles(key));
		}
		if(uploadFileList.get(0).getSize() != 0) {
		if (!uploadFileList.isEmpty() && uploadFileList.size() > 0) {
			fileList = fileUtil.saveAllFiles(uploadFile);
			  if (fileList.isEmpty()) { 
				  model.addAttribute("message","첨부파일에 문제가 생겼습니다. 관리자에 문의하세요."); 
				  model.addAttribute("url", "list.ivs");
				  return "cmmn/message";
			  }
			 
		}
		}
		int result = contractservice.save(vo, params, fileList);

		if (result > 0) {
			model.addAttribute("message", messageSource.getMessage("saleplan.insert.success", null, Locale.getDefault()));
			model.addAttribute("url", "list.ivs");

		} else {
			model.addAttribute("message", messageSource.getMessage("common.insert.fail", null, Locale.getDefault()));
			model.addAttribute("url", "view.ivs?seq=" + vo.getSeq());
		}
		// return "redirect:sampleList?pageNo=1";
		return "cmmn/message";
	}


	/**
	 * 글을 수정한다.
	 * @param SampleVO - 등록할 정보가 담긴 EstimateVO
	 * @param model
	 * @return "sampleSave"
	 * @exception Exception
	 */
	@RequestMapping("/update.proc")
	public String update(HttpServletRequest request, ContractVO vo, ModelMap model, @RequestParam Map<String, Object> params) throws Exception {
		System.out.println("update");
		System.out.println("Seq====" + vo.getSeq());
		//첨부파일 관련
				params.put("seq",vo.getSeq() );
				params.put("section","contract");
				
				List<MultipartFile> uploadFileList = new ArrayList<MultipartFile>();
				Map<String, List<MultipartFile>> uploadFile = new HashMap<String, List<MultipartFile>>();
				List<FileVO> fileList = null;
				
				MultipartRequest multipartReq = (MultipartRequest) request;
				Iterator<String> filenames = multipartReq.getFileNames();
				while (filenames.hasNext()) {
					String key = (String) filenames.next();
					uploadFile.put(key, multipartReq.getFiles(key));
					uploadFileList.addAll(multipartReq.getFiles(key));
				}
				if(uploadFileList.get(0).getSize() != 0) {
				if (uploadFileList != null && uploadFileList.size() > 0) {
					fileList = fileUtil.saveAllFiles(uploadFile);
					  if (fileList.isEmpty()) { 
						  model.addAttribute("message","첨부파일에 문제가 생겼습니다. 관리자에 문의하세요."); 
						  //model.addAttribute("action", "close");
						  return "cmmn/message";
					  }
					 
				}
				}
		int result = contractservice.update(vo, params, fileList);

		if (result > 0) {
			model.addAttribute("message", messageSource.getMessage("saleplan.update.success", null, Locale.getDefault()));
			model.addAttribute("url", "list.ivs");

		} else {
			model.addAttribute("message", messageSource.getMessage("common.update.fail", null, Locale.getDefault()));
			model.addAttribute("url", "view.ivs?seq=" + vo.getSeq());
		}
		return "cmmn/message";

	}
	
	/**
	 * 글을 삭제한다.
	 * @param SampleVO - 등록할 정보가 담긴 EstimateVO
	 * @param model
	 * @return "sampleSave"
	 * @exception Exception
	 */
	@RequestMapping("/delete.proc")
	public String delete(HttpServletRequest request, ContractVO vo, ModelMap model) throws Exception {
		System.out.println("delete");
		System.out.println("Seq====" + vo.getSeq());
		int result = contractservice.delete(vo);

		if (result > 0) {
			model.addAttribute("message", messageSource.getMessage("common.delete.success", null, Locale.getDefault()));
			model.addAttribute("url", "list.ivs");

		} else {
			model.addAttribute("message", messageSource.getMessage("common.delete.fail", null, Locale.getDefault()));
			model.addAttribute("url", "list.ivs");
		}
		return "cmmn/message";

	}


	/**
	 * 상세화면
	 * @param SampleVO - 등록할 정보가 담긴 SampleVO
	 * @param model
	 * @return "sampleView"
	 * @exception Exception
	 */
	@RequestMapping("/view.ivs")
	public String sampleView(HttpServletRequest request, ContractVO vo, ModelMap model) throws Exception {
		CmmnMap param = new CmmnMap();
		param.put("seq", vo.getSeq());
		param.put("section","contract");
		vo = contractservice.getView(vo);
		model.addAttribute("comment", cmmnService.selectList("commentList", param));
		model.addAttribute("result", vo);
		model.addAttribute("file", cmmnService.selectList("viewFile", param));
		return "contract/view";

	}
	
	/**
	 * 글을 수정한다.
	 * @param SampleVO - 등록할 정보가 담긴 SampleVO
	 * @param model
	 * @return "sampleSave"
	 * @exception Exception
	 */
	@RequestMapping("/updateReqeust.proc")
	public String updateConsent(HttpServletRequest request, ContractVO vo, ModelMap model) throws Exception {
		System.out.println("consent==="+vo.getConsent());
//		if(vo.getRequest() > 0)
//			vo.setRequest(0);
//		else
//			vo.setRequest(1);
		int result = contractservice.updateReqeust(vo);
		if (result > 0 && vo.getRequest() == 1) {
			model.addAttribute("message", messageSource.getMessage("saleplan.consent.success", null, Locale.getDefault()));
			model.addAttribute("url", "list.ivs");

		} else if (result > 0 && vo.getRequest() == 0) {
			model.addAttribute("message", messageSource.getMessage("saleplan.consentcancel.success", null, Locale.getDefault()));
			model.addAttribute("url", "list.ivs");

		}else {
			model.addAttribute("message", messageSource.getMessage("saleplan.consent.fail", null, Locale.getDefault()));
			model.addAttribute("url", "view.ivs?seq=" + vo.getSeq());
		}
		return "cmmn/message";

	}


	/**
	 * 상세화면
	 * @param CmmnMap - 엑셀로 만들 CmmnMap
	 * @param model
	 * @return "sampleExcel"
	 * @exception Exception
	 */
	@RequestMapping("/estimateExcel")
	public ModelAndView sampleExcel(HttpServletRequest request, HttpServletResponse response, ModelMap model,
			@RequestParam Map<String, String> params) throws Exception {

		CmmnMap param = new CmmnMap();
		param.putAll(params);

		logger.info("----------excel param-----------------------");
		logger.debug("");
		logger.debug(param.toString());
		logger.debug("");
		logger.debug("----------excel param-----------------------");

//		int pageSize = this.propertiesService.getInt("pageSize"); // 페이지 크기
//		int pageUnit = this.propertiesService.getInt("pageUnit"); // 페이지 목록갯수
//
//       	String key = "searchKeyword";
//    	param.put(key, param.getString(key)); // 검색어
//    	
//		PaginationInfo paginationInfo = new PaginationInfo();
//		key = "cPage";
//		paginationInfo.setCurrentPageNo(param.getInt(key) > 0 ? param.getInt(key) : 1);
//		key = "searchPageSize";
//		paginationInfo.setRecordCountPerPage(param.getInt(key) > 0 ? param.getInt(key) : pageUnit);	
//		paginationInfo.setPageSize(pageSize);
//		param.put("firstIndex",paginationInfo.getFirstRecordIndex());
//		param.put("lastIndex",paginationInfo.getLastRecordIndex());
//		param.put("recordCountPerPage",paginationInfo.getRecordCountPerPage());
//		
//		List<CmmnMap>  list = cmmnService.selectList("sample.excel", param);
//		
//		String[] head ={"번호","제목","등록일"};
//		param.put("header", head);		  // Excel 상단
//		param.put("excelName","샘플");    // Excel 파일명
//		param.put("list", list);          // Excel Data

		return new ModelAndView("cmmnExcelView", "data", param);
	}

}
