package com.example.datlichcattoc.Database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface CartDataSource {

    Single<Long> sumPrice(String userPhone);

    Flowable<List<CartItem>> getAllItemFromCart(String userPhone);

    Single<Integer> countItemInCart(String userPhone);

    Flowable<CartItem> getProductInCart(String productId, String userPhone);

    Completable insert(CartItem...carts);

    Single<Integer> update(CartItem cart);

    Single<Integer> delete(CartItem cartItem);

    Single<Integer> clearCart(String userPhone);
}
