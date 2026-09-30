package com.vadastore.music_store_api.service;

import com.vadastore.music_store_api.domain.User;
import com.vadastore.music_store_api.record.SellerRequest;
import com.vadastore.music_store_api.record.SellerResponse;
import com.vadastore.music_store_api.repository.ProductRepository;
import com.vadastore.music_store_api.repository.SellerRepository;
import com.vadastore.music_store_api.domain.Product;
import com.vadastore.music_store_api.domain.Seller;
import com.vadastore.music_store_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

            public SellerResponse registerSeller(SellerRequest request, Long authenticatedId) {

                User user = userRepository.findById(authenticatedId).orElseThrow(() -> new EntityNotFoundException("USer not found"));

                Seller seller = request.dtoToEntity(user);
                sellerRepository.save(seller);

                return new SellerResponse(seller.getId(), seller.getStoreName());

        }

        public Seller updateAttributes(Long id, SellerRequest updatedData) {
            Seller seller = sellerRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Couldn't find user"));


            if (updatedData.storeName() != null) {
                seller.setStoreName(updatedData.storeName());
            }

            if (updatedData.postalCode() != null) {
                seller.setPostalCode(updatedData.postalCode());
            }

            return sellerRepository.save(seller);
        }

        public void deactivateSeller(Long id) {
            Seller sellerFound = sellerRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
            sellerFound.setActive(false);

         sellerRepository.save(sellerFound);
        }


        public void vincularProdutoAoSeller(Product product, Seller seller) {
            Product productFound = productRepository.findById(product.getId()).orElseThrow(() -> new EntityNotFoundException("Couldn't find product"));
            Seller sellerFound = sellerRepository.findById(seller.getId()).orElseThrow(() -> new EntityNotFoundException("Couldn't find user"));

            productFound.setSeller(sellerFound);

            productRepository.save(productFound);
        }
}
