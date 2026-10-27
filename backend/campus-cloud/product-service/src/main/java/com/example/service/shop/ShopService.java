package com.example.service.shop;

import com.example.dto.product.MyProductQueryDTO;
import com.example.dto.product.ProductCreateDTO;
import com.example.result.PageResult;
import com.example.vo.product.ProductDetailVO;
import com.example.vo.product.UserProductStatsVO;

public interface ShopService {

    Long createProduct(ProductCreateDTO dto);

    void updateProduct(Long id, ProductCreateDTO dto);

    void deleteProduct(Long id);

    void offlineProduct(Long id);

    void onlineProduct(Long id);

    PageResult listMyProducts(MyProductQueryDTO query);

    ProductDetailVO getMyProductDetail(Long id);

    UserProductStatsVO getMyStats();
}