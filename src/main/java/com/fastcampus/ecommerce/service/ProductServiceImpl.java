package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.common.errors.ResourceNotFoundException;
import com.fastcampus.ecommerce.entity.Category;
import com.fastcampus.ecommerce.entity.Product;
import com.fastcampus.ecommerce.entity.ProductCategory;
import com.fastcampus.ecommerce.entity.ProductCategory.ProductCategoryId;
import com.fastcampus.ecommerce.model.CategoryResponse;
import com.fastcampus.ecommerce.model.PaginatedProductResponse;
import com.fastcampus.ecommerce.model.ProductRequest;
import com.fastcampus.ecommerce.model.ProductResponse;
import com.fastcampus.ecommerce.repository.CategoryRepository;
import com.fastcampus.ecommerce.repository.ProductCategoryRepository;
import com.fastcampus.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;

    @Override
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(product -> {
            List<CategoryResponse> productCategories = getProductCategories(product.getProductId());
            return ProductResponse.fromProductAndCategories(product, productCategories);
        }).toList();
    }

    @Override
    public Page<ProductResponse> findByPage(Pageable pageable) {
        return productRepository.findByPageable(pageable)
                .map(
                product -> {
                    List<CategoryResponse> productCategories = getProductCategories(product.getProductId());
                    return ProductResponse.fromProductAndCategories(product, productCategories);
                }
        );
    }

    @Override
    public Page<ProductResponse> findByNameAndPageable(String name, Pageable pageable) {
        name = "%" + name + "%";
        name = name.toLowerCase();
        return productRepository.findByNamePageable(name, pageable)
                .map(
                        product -> {
                            List<CategoryResponse> productCategories = getProductCategories(product.getProductId());
                            return ProductResponse.fromProductAndCategories(product, productCategories);
                        }
                );
    }

    @Override
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        List<CategoryResponse> productCategories = getProductCategories(id);
        return ProductResponse.fromProductAndCategories(product, productCategories);
    }

    @Override
    public ProductResponse create(ProductRequest productRequest) {
        log.info("MASUK A");
        List<Category> categories = getCategoriesByIds(productRequest.getCategoryIds());
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .stockQuantity(productRequest.getStockQuantity())
                .weight(productRequest.getWeight())
                .build();
        Product createdProduct = productRepository.save(product);

        log.info("MASUK B");
        List<ProductCategory> productCategories = categories.stream()
                .map(category -> {
                    ProductCategory productCategory = ProductCategory.builder().build();
                    ProductCategoryId productCategoryId = new ProductCategoryId();
                    productCategoryId.setCategoryId(category.getCategoryId());
                    productCategoryId.setProductId(createdProduct.getProductId());
                    productCategory.setId(productCategoryId);
                    return productCategory;
                }).toList();
        productCategoryRepository.saveAll(productCategories);

        log.info("MASUK C");
        List<CategoryResponse> categoryResponseList = categories.stream().map(CategoryResponse::fromCategory).toList();
        return ProductResponse.fromProductAndCategories(createdProduct, categoryResponseList);
    }

    @Override
    public ProductResponse update(Long id, ProductRequest productRequest) {
        Product existingProduct = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        List<Category> categories = getCategoriesByIds(productRequest.getCategoryIds());
        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setPrice(productRequest.getPrice());
        existingProduct.setStockQuantity(productRequest.getStockQuantity());
        existingProduct.setWeight(productRequest.getWeight());
        productRepository.save(existingProduct);

        List<ProductCategory> exitingProductCategories = productCategoryRepository.findCategoriesByProductId(id);
        productCategoryRepository.deleteAll(exitingProductCategories);

        List<ProductCategory> productCategories = categories.stream()
                .map(category -> {
                    ProductCategory productCategory = ProductCategory.builder().build();
                    ProductCategoryId productCategoryId = new ProductCategoryId();
                    productCategoryId.setCategoryId(category.getCategoryId());
                    productCategoryId.setProductId(id);
                    productCategory.setId(productCategoryId);
                    return productCategory;
                }).toList();
        productCategoryRepository.saveAll(productCategories);

        List<CategoryResponse> categoryResponseList = categories.stream().map(CategoryResponse::fromCategory).toList();
        return ProductResponse.fromProductAndCategories(existingProduct, categoryResponseList);
    }

    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        List<ProductCategory> exitingProductCategories = productCategoryRepository.findCategoriesByProductId(id);
        productCategoryRepository.deleteAll(exitingProductCategories);
        productRepository.delete(product);
    }

    @Override
    public PaginatedProductResponse convertProductPage(Page<ProductResponse> responses) {
        return PaginatedProductResponse.builder()
                .data(responses.getContent())
                .pageNo(responses.getNumber())
                .pageSize(responses.getSize())
                .totalElement(responses.getTotalElements())
                .totalPage(responses.getTotalPages())
                .last(responses.isLast())
                .build();
    }

    private List<Category> getCategoriesByIds(List<Long> categoryIds) {
        log.info("MASUK getCategoriesByIds");
        return categoryIds.stream()
                .map(categoryId -> categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found for id " + categoryIds))
                )
                .toList();
    }

    private List<CategoryResponse> getProductCategories(Long productId) {
        List<ProductCategory> productCategories = productCategoryRepository.findCategoriesByProductId(productId);
        List<Long> categoryIds = productCategories.stream().map(productCategory -> productCategory.getId().getCategoryId()).toList();
        return categoryRepository.findAllById(categoryIds).stream().map(CategoryResponse::fromCategory).toList();
    }
}
