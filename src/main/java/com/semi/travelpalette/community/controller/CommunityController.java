package com.semi.travelpalette.community.controller;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.semi.travelpalette.common.domain.Like;
import com.semi.travelpalette.common.domain.PageInfo;
import com.semi.travelpalette.community.domain.Community;
import com.semi.travelpalette.community.domain.Reply;
import com.semi.travelpalette.community.service.CommunityService;
import com.semi.travelpalette.community.service.ReplyService;

@Controller
@RequestMapping("/community")
public class CommunityController {
    
    @Autowired
    private CommunityService cService;
    @Autowired
    private ReplyService rService;
    
    @RequestMapping(value="/qList.tp", method=RequestMethod.GET)
    public ModelAndView goBoardListPage(ModelAndView mv
            , @RequestParam(value= "page", required = false, defaultValue="1") Integer curruntPage
            , @RequestParam(value= "boardType", required = false, defaultValue="QnABoard") String boardType) {
        
        try {
            int totalCount = cService.getListCountByBoardType(boardType);
            PageInfo pInfo = this.getPageInfo(curruntPage, totalCount, boardType);
            List<Community> cList = cService.selectCommunityList(pInfo);

            if (cList.size() > 0) {
                mv.addObject("cList", cList).addObject("pInfo", pInfo);
                mv.setViewName("community/questionList");
                return mv;
            } else {
                mv.addObject("msg", "리스트 불러오기에 실패하였습니다.");
                mv.addObject("url", "/");
                mv.setViewName("common/errorPage");
                return mv;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            mv.setViewName("/");
            return mv;
        }
    }
    
    @RequestMapping(value="/certify.tp", method=RequestMethod.GET)
    public ModelAndView goCertifyListPage(ModelAndView mv
            , @RequestParam(value= "page", required = false, defaultValue="1") Integer curruntPage
            , @RequestParam(value= "boardType", required = false, defaultValue="travelcertify") String boardType) {
        
        try {
            int totalCount = cService.getListCountByBoardType(boardType);
            PageInfo pInfo = this.getPageInfo(curruntPage, totalCount, boardType);
            List<Community> cList = cService.selectCommunityList(pInfo);

            if (cList.size() > 0) {
                mv.addObject("cList", cList).addObject("pInfo", pInfo);
                mv.setViewName("community/travelcertifyList");
                return mv;
            } else {
                mv.addObject("msg", "리스트 불러오기에 실패하였습니다.");
                mv.addObject("url", "/");
                mv.setViewName("common/errorPage");
                return mv;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            mv.setViewName("/");
            return mv;
        }
    }
    
    @RequestMapping(value="/detail.tp", method=RequestMethod.GET)
    public ModelAndView goBoardDatailPage(ModelAndView mv
            , @RequestParam("boardType") String boardType
            , @RequestParam("boardNo") int boardNo
            , HttpSession session) {
        
        try {
            Community cOne = new Community(boardNo, boardType);
            Community community = cService.selectOneByClass(cOne);
            community.setViewCount(community.getViewCount()+1);
            cService.updateViewCount(community);
            List<Reply> rList = rService.selectReplyList(cOne);
            String userId = (String)session.getAttribute("userId");
            if(userId != null) {            	
            	Like like = new Like(boardNo, boardType, userId);
            	Like cLike = cService.selectLikeByClass(like);
            	if(cLike != null) {
            		mv.addObject("likeId", userId);
            	}
            }

            if (!community.getBoardTitle().equals("")) {
                mv.addObject("community", community).addObject("rList", rList);
                mv.setViewName("community/detail");
                return mv;
            } else {
                mv.addObject("msg", "게시물 상세보기에 실패하였습니다.");
                mv.addObject("url", "/");
                mv.setViewName("common/errorPage");
                return mv;
            } 

        } catch (Exception e) {
            System.out.println(e.getMessage());
            mv.setViewName("/");
            return mv;
        }
    }
    
    @GetMapping("/insert.tp")
    public ModelAndView goInsertBoardPage(ModelAndView mv) {
        
        mv.setViewName("community/insert");
        return mv;
    }
    
    
    @PostMapping("/insert.tp")
    public ModelAndView insertBoard(ModelAndView mv
            ,@ModelAttribute Community community
            ,HttpSession session
            , @RequestParam(value="uploadFile", required=false) MultipartFile uploadFile
			, HttpServletRequest request) {
        
        try {
        	if(uploadFile != null && !uploadFile.getOriginalFilename().equals("")) {
				Map<String, Object> cMap = this.saveFile(request, uploadFile);
				community.setBoardFileName((String)cMap.get("fileName"));
				community.setBoardFileRename((String)cMap.get("fileRename"));
				community.setBoardFilePath((String)cMap.get("filePath"));
				community.setBoardFileLength((long)cMap.get("fileLength"));
			}
            int maxNo = cService.selectMaxNo();
            community.setUserId((String)session.getAttribute("userId"));
            community.setUserNickname((String)session.getAttribute("userNickname"));
            int result = cService.insertBoard(community);
            
            if(result > 0) {
                mv.setViewName("redirect:/community/detail.tp?boardType="+community.getBoardType()+"&boardNo="+(maxNo+1));
                return mv;
            }else {
                mv.addObject("msg", "게시물 등록에 실패하였습니다.");
                mv.addObject("url", "/");
                mv.setViewName("common/errorPage");
                return mv;
            } 
        } catch (Exception e) {
            System.out.println(e.getMessage());
            mv.setViewName("redirect:/");
            return mv;
        }
    }
    
    @RequestMapping(value="/modify.tp", method=RequestMethod.GET)
    public ModelAndView goBoardModifyPage(ModelAndView mv
            , @RequestParam("boardType") String boardType
            , @RequestParam("boardNo") int boardNo
            ,HttpSession session) {
        
        try {
            Community cOne = new Community(boardNo, boardType);
            Community community = cService.selectOneByClass(cOne);

            if (!community.getBoardTitle().equals("")) {
                mv.addObject("community", community).addObject("userId", (String)session.getAttribute("userId"))
                .addObject("userNickname", (String)session.getAttribute("userNickname"));
                mv.setViewName("community/modify");
                return mv;
            } else {
                mv.addObject("msg", "게시물 수정페이지 이동에 실패하였습니다.");
                mv.addObject("url", "/");
                mv.setViewName("common/errorPage");
                return mv;
            } 

        } catch (Exception e) {
            System.out.println(e.getMessage());
            mv.setViewName("community/detail?boardType"+boardType+"&boardNo="+boardNo);
            return mv;
        }
    }
    
    @PostMapping("/modify.tp")
    public ModelAndView modifyBoard(ModelAndView mv
    		, HttpServletRequest request
            ,@ModelAttribute Community community
            , @RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile) {
        
        try {
        	if(uploadFile != null && !uploadFile.isEmpty()) {
				
				String fileName = community.getBoardFileRename();
				if(community.getBoardFileName() != null) {
					this.deleteFile(request, fileName);
				}
				Map<String, Object> cMap= this.saveFile(request, uploadFile);
				community.setBoardFileName((String)cMap.get("fileName"));
				community.setBoardFileRename((String)cMap.get("fileRename"));
				community.setBoardFilePath((String)cMap.get("filePath"));
				community.setBoardFileLength((long)cMap.get("fileLength"));
			}
            int result = cService.updateBoard(community);
            
            if(result > 0) {
            	 mv.setViewName("redirect:/community/detail.tp?boardType="+community.getBoardType()+"&boardNo="+community.getBoardNo());
                return mv;
            }else {
                mv.addObject("msg", "게시물 수정에 실패하였습니다.");
                mv.addObject("url", "/");
                mv.setViewName("common/errorPage");
                return mv;
            } 
        } catch (Exception e) {
        	mv.addObject("msg", "뭐가 문제일까용?");
            mv.addObject("url", "/");
            mv.setViewName("common/errorPage");
            return mv;
        }
    }
    
    @RequestMapping(value="/delete.tp", method=RequestMethod.GET)
    public ModelAndView deleteBoard(ModelAndView mv
            ,@ModelAttribute Community community) {
        
        try {
            int result = cService.deleteBoard(community);
            
            if(result > 0) {
                mv.setViewName("redirect:/community/qList.tp");
                return mv;
            }else {
                mv.addObject("msg", "게시물 삭제에 실패하였습니다.");
                mv.addObject("url", "/");
                mv.setViewName("community/errorPage");
                return mv;
            } 
        } catch (Exception e) {
            System.out.println(e.getMessage());
            mv.setViewName("/");
            return mv;
        }
    }
    
    public PageInfo getPageInfo(int curruntPage, int totalCount, String boardType) {

        PageInfo pi = null;
        int recordCountPerPage = 10;
        int naviCountPerPage = 5;
        int naviTotalCount;
        int startNavi;
        int endNavi;

        naviTotalCount = (int)((double) totalCount / recordCountPerPage + 0.9);

        startNavi = (((int) ((double) curruntPage / naviCountPerPage + 0.9)) - 1) * naviCountPerPage + 1;

        endNavi = startNavi + naviCountPerPage - 1;
        if (endNavi > naviTotalCount) {
            endNavi = naviTotalCount;
        }

        pi = new PageInfo(curruntPage, recordCountPerPage, naviCountPerPage, startNavi, endNavi, totalCount,
                naviTotalCount, boardType);
        return pi;
    }
    
    public Map<String, Object> saveFile(HttpServletRequest request, MultipartFile uploadFile) throws IllegalStateException, IOException {
		Map<String, Object> fileMap = new HashMap<String, Object>();
		// resources 경로 구하기
		String root = request.getSession().getServletContext().getRealPath("resources");
		// 파일 저장경로 구하기
		String savePath = root + "\\cuploadFiles";
		//	파일 이름 구하기
		String fileName = uploadFile.getOriginalFilename();
		// 파일 확장자 구하기
		String extension 
			= fileName.substring(fileName.lastIndexOf(".")+1);
		// 시간으로 파일 리네임하기
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String fileRename = sdf.format(new Date(System.currentTimeMillis()))+"."+extension;
		// 파일 저장 전 폴더 만들기
		File saveFolder = new File(savePath);
		if(!saveFolder.exists()) {
			saveFolder.mkdir();
		}
		// 파일 저장
		File saveFile = new File(savePath+"\\"+fileRename);
		uploadFile.transferTo(saveFile);
		long fileLength = uploadFile.getSize();
		fileMap.put("fileName", fileName);
		fileMap.put("fileRename", fileRename);
		fileMap.put("filePath", "../resources/cuploadFiles/"+fileRename);
		fileMap.put("fileLength", fileLength);
		return fileMap;
	}
    
    public void deleteFile(HttpServletRequest request, String fileName) {
		String root = request.getSession().getServletContext().getRealPath("resources");
		String delFilepath = root+"\\cuploagFiles\\"+fileName;
		File file = new File(delFilepath);
		if(file.exists()) {
			file.delete();
		}
	}
}