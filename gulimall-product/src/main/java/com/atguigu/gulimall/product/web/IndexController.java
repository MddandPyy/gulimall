package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


/**
 * 1 如果只是使用@RestController注解Controller
 *
 *  Controller中的方法无法返回jsp页面，或者html，
 *  配置的视图解析器 InternalResourceViewResolver不起作用，
 *  返回的内容就是Return 里的内容（String/JSON）。
 *
 * 2 如果只是使用@Controller注解Controller
 *
 *  若需要返回到指定页面，
 *  则需要用 @Controller配合视图解析器InternalResourceViewResolver才行。
 *
 *  若需要返回JSON，XML或自定义mediaType内容到页面，
 *  则需要在对应的方法上加上@ResponseBody注解。
 */
@Controller
public class IndexController {

    @Resource
    private CategoryService categoryService;


    @GetMapping(value = {"/","index.html"})
    private String indexPage(Model model) {

        //1、查出所有的一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
        model.addAttribute("categories",categoryEntities);

        return "index";
    }

    //index/json/catalog.json
    @GetMapping(value = "/index/catalog.json")
    //只返回json数据
    @ResponseBody
    public Map<String, List<Catelog2Vo>> getCatalogJson() {

        Map<String, List<Catelog2Vo>> catalogJson = categoryService.getCatalogJson();

        return catalogJson;

    }


    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }





}
