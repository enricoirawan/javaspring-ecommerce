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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final RateLimitingService rateLimitingService;

    private final String PRODUCT_CACHE_KEY = "products";
    private final CacheService cacheService;

    @Override
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(product -> {
            List<CategoryResponse> productCategories = getProductCategories(product.getProductId());
            return ProductResponse.fromProductAndCategories(product, productCategories);
        }).toList();
    }

    @Override
    public Page<ProductResponse> findByPage(Pageable pageable) {
        return rateLimitingService.executeWithRateLimit("product_listing", () -> productRepository.findByPageable(pageable)
                .map(
                        product -> {
                            List<CategoryResponse> productCategories = getProductCategories(product.getProductId());
                            return ProductResponse.fromProductAndCategories(product, productCategories);
                        }
                ));
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
        String cacheKey = PRODUCT_CACHE_KEY + id;
        Optional<ProductResponse> cachedProduct = cacheService.get(cacheKey, ProductResponse.class);

        if(cachedProduct.isPresent()) {
            return cachedProduct.get();
        }

        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        List<CategoryResponse> productCategories = getProductCategories(id);
        ProductResponse productResponse = ProductResponse.fromProductAndCategories(product, productCategories);
        cacheService.put(cacheKey, productResponse);
        return productResponse;
    }

    @Override
    public ProductResponse create(ProductRequest productRequest, Long userId) {
        List<Category> categories = getCategoriesByIds(productRequest.getCategoryIds());
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .stockQuantity(productRequest.getStockQuantity())
                .weight(productRequest.getWeight())
                .userId(userId)
//                .userId(productRequest.getUser().getUserId())
                .build();
        Product createdProduct = productRepository.save(product);

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
        List<CategoryResponse> categoryResponseList = categories.stream().map(CategoryResponse::fromCategory).toList();

        String cachedkey = PRODUCT_CACHE_KEY + createdProduct.getProductId();
        ProductResponse productResponse = ProductResponse.fromProductAndCategories(createdProduct, categoryResponseList);
        cacheService.put(cachedkey, productResponse);
        return productResponse;
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

        String cachedkey = PRODUCT_CACHE_KEY + id;
        cacheService.evict(cachedkey);
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
