package com.example.service.adminProduct;

import com.example.dto.adminProduct.AdminProductQueryDTO;
import com.example.dto.adminProduct.OfflineDTO;
import com.example.dto.adminProduct.ProductAuditDTO;
import com.example.result.PageResult;
import com.example.vo.product.ProductDetailVO;

public interface AdminProductService {

    PageResult listProducts(AdminProductQueryDTO query);

    ProductDetailVO getProductDetail(Long id);

    void auditProduct(Long id, ProductAuditDTO dto);

    void forceOffline(Long id, OfflineDTO dto);

    void deleteProduct(Long id);
}