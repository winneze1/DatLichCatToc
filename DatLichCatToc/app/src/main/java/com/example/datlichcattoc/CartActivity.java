package com.example.datlichcattoc;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datlichcattoc.Adapter.MyCartAdapter;
import com.example.datlichcattoc.Common.Common;
import com.example.datlichcattoc.Database.CartDataSource;
import com.example.datlichcattoc.Database.CartDatabase;
import com.example.datlichcattoc.Database.LocalCartDataSource;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CartActivity extends AppCompatActivity {

    MyCartAdapter adapter;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    CartDataSource cartDataSource;

    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.btn_clear_cart)
    Button btn_clear_cart;

    @OnClick(R.id.btn_clear_cart)
    void clearCart(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Clear Cart")
                .setMessage("Do you really want to clear cart???")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //DatabaseUtils.clearCart(cartDatabase);
                        cartDataSource.clearCart(Common.currentUser.getPhoneNumber())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<Integer>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onSuccess(Integer integer) {
                                        //load hết cart sau khi clear
                                        Toast.makeText(CartActivity.this, "Cart has been clear :D", Toast.LENGTH_SHORT).show();
                                        compositeDisposable.add(
                                                cartDataSource.getAllItemFromCart(Common.currentUser.getPhoneNumber())
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(cartItemList -> {
                                                            //Sau khi xong chi cần tính tổng
                                                            //sau khi xóa hết item, update tổng giá
                                                            cartDataSource.sumPrice(Common.currentUser.getPhoneNumber())
                                                                    .subscribeOn(Schedulers.io())
                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                    .subscribe(updatePirce());
                                                        }, throwable -> {
                                                            Toast.makeText(CartActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                        })
                                        );
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Toast.makeText(CartActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                        //cập nhật adapter
                        //DatabaseUtils.getAllCart(cartDatabase, this);
                        //DatabaseUtils.sumCart(cartDatabase, this);
                        getAllCart();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private SingleObserver<? super Long> updatePirce() {
        return new SingleObserver<Long>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(Long aLong) {
                txt_total_price.setText(new StringBuilder("$").append(aLong));
            }

            @Override
            public void onError(Throwable e) {
                if (e.getMessage().contains("Query returned empty"))
                    txt_total_price.setText("");
                else
                    Toast.makeText(CartActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                finish();//đóng activity
            }
        };
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        ButterKnife.bind(CartActivity.this);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
        //getAllCart(cartDatabase,this);
        getAllCart();

        //View
        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler_cart.setLayoutManager(linearLayoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
    }

    private void getAllCart() {
        compositeDisposable.add(cartDataSource.getAllItemFromCart(Common.currentUser.getPhoneNumber())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItemList -> {
                    adapter = new MyCartAdapter(this, cartItemList);
                    recycler_cart.setAdapter(adapter);

                    //up giá
                    cartDataSource.sumPrice(Common.currentUser.getPhoneNumber())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(updatePirce());
                }, throwable -> {
                    Toast.makeText(this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }


    @Override
    protected void onDestroy() {
        if (adapter!=null)
            adapter.onDestroy();
        compositeDisposable.clear();
        super.onDestroy();
    }
}
