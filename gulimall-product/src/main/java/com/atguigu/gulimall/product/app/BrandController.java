package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 品牌
 *
 * @author lzp
 * @email lzp@gmail.com
 * @date 2022-03-09 09:38:07
 */
@RestController
@RequestMapping("product/brand")

public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    @GetMapping("/infos")
    public R info(@RequestParam("brandIds") List<Long> brandIds) {
        List<BrandEntity> brand = brandService.getBrandByIds(brandIds);
        return R.ok().put("data", brand);
    }

//    /**
//     * 保存
//     */
//    @RequestMapping("/save")
//    //@RequiresPermissions("product:brand:save")
//    //此处添加@Valid，告诉springmvc在BrandEntity中有需要校验的字段，具体校验通过注解，添加在实体的字段上
//    public R save(@Valid @RequestBody BrandEntity brand, BindingResult result){
//		if(result.hasErrors()){
//		    Map<String,Object> map = new HashMap<>();
//		    result.getFieldErrors().forEach((item)->{
//		        String message = item.getDefaultMessage();
//                String field = item.getField();
//                map.put(field,message);
//            });
//		   return R.error(400,"提交数据不合法").put("data",map);
//        }else{
//            brandService.save(brand);
//        }
//
//        return R.ok();
//    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    //此处添加@Valid，告诉springmvc在BrandEntity中有需要校验的字段，具体校验通过注解，添加在实体的字段上
    public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand){
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){


		brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update/status")
    //@RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }
    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
