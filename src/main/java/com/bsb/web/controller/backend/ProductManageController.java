package com.bsb.web.controller.backend;

import com.bsb.common.Const;
import com.bsb.common.ResponseCode;
import com.bsb.common.ServerResponse;
import com.bsb.properties.MallProperties;
import com.bsb.web.pojo.Product;
import com.bsb.web.pojo.User;
import com.bsb.web.service.IFileService;
import com.bsb.web.service.IProductService;
import com.bsb.web.service.IUserService;
import com.bsb.web.vo.ProductDetiailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zeng
 */
@RestController
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private IUserService userService;
    @Autowired
    private IProductService productService;
    @Autowired
    private IFileService fileService;
    @Autowired
    private MallProperties mallProperties;

    @PostMapping("/save")
    public ServerResponse saveProduct(HttpSession session, Product product) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请重新登录");
        }

        if (userService.checkAdminRole(user).isSuccess()) {
            //增加产品
            return productService.saveOrUpdateProduct(product);
        } else {
            return ServerResponse.createByErrorMsg("无权限操作");
        }

    }

    @PostMapping("/updateProductStatus")
    public ServerResponse updateProductStatus(HttpSession session, Integer productId, Integer status) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请重新登录");
        }

        if (userService.checkAdminRole(user).isSuccess()) {
            //修改产品上架状态
            return productService.updateProductStatus(productId, status);
        } else {
            return ServerResponse.createByErrorMsg("无权限操作");
        }

    }

    @PostMapping("/getDetail")
    public ServerResponse<ProductDetiailVo> getDetail(HttpSession session, Integer productId) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请重新登录");
        }

        if (userService.checkAdminRole(user).isSuccess()) {

            return productService.manageProductDetail(productId);
        } else {
            return ServerResponse.createByErrorMsg("无权限操作");
        }
    }

    @PostMapping("/getList")
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请重新登录");
        }

        if (userService.checkAdminRole(user).isSuccess()) {

            return productService.getProductList(pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMsg("无权限操作");
        }
    }

    @PostMapping("/search")
    public ServerResponse searchProduct(HttpSession session, String productName, Integer productId, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请重新登录");
        }

        if (userService.checkAdminRole(user).isSuccess()) {

            return productService.searchProduct(productName, productId, pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMsg("无权限操作");
        }
    }

    @PostMapping("/upload")
    public ServerResponse upload(HttpSession session,
                                 @RequestParam(value = "file", required = false) MultipartFile file, HttpServletRequest request) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请重新登录");
        }

        if (userService.checkAdminRole(user).isSuccess()) {

            String path = request.getSession().getServletContext().getRealPath("upload");

            String targetFileName = fileService.upload(file, path);
            String url = mallProperties.getFtp().getServerHttpPrefix() + targetFileName;

            Map fileMap = new HashMap();
            fileMap.put("uri", targetFileName);
            fileMap.put("url", url);

            return ServerResponse.createBySuccess(fileMap);

        } else {
            return ServerResponse.createByErrorMsg("无权限操作");
        }
    }

    @PostMapping("/richTextUpload")
    public Map richTextImgUpload(HttpSession session, @RequestParam(value = "file", required = false) MultipartFile file,
                                 HttpServletRequest request, HttpServletResponse response) {

        Map resultMap = new HashMap();

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            resultMap.put("success", false);
            resultMap.put("msg", "请登录管理员账户");

            return resultMap;
        }

        //富文本中做自己要求simditor

        if (userService.checkAdminRole(user).isSuccess()) {

            String path = request.getSession().getServletContext().getRealPath("upload");

            String targetFileName = fileService.upload(file, path);

            if (StringUtils.isBlank(targetFileName)) {
                resultMap.put("success", false);
                resultMap.put("msg", "上传失败");
                return resultMap;
            }

            String url = mallProperties.getFtp().getServerHttpPrefix() + targetFileName;

            resultMap.put("success", true);
            resultMap.put("msg", "上传成功");
            resultMap.put("file_path", url);

            response.addHeader("Access-Control-Allow-Headers", "X-File-Name");
            return resultMap;

        } else {
            resultMap.put("success", false);
            resultMap.put("msg", "无权限操作");

            return resultMap;
        }
    }
}
