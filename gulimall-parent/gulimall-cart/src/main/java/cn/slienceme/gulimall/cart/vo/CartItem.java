package cn.slienceme.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * 购物项内容
 */
public class CartItem {

    private Long skuId;             //   商品id
    private Boolean check = true;   // 是否选中
    private String title;           // 商品标题
    private String image;           // 商品图片
    private List<String> skuAttr;   // 商品属性
    private BigDecimal price;       // 商品价格
    private Integer count;          // 商品数量
    private BigDecimal totalPrice;  // 商品总价

    /**
     * 获取商品总价
     *
     * @return
     */
    public BigDecimal getTotalPrice() {
        this.totalPrice = price.multiply(new BigDecimal(count.toString()));
        return totalPrice;
    }

    public CartItem() {
    }

    public CartItem(Long skuId, Boolean check, String title, String image, List<String> skuAttr, BigDecimal price, Integer count, BigDecimal totalPrice) {
        this.skuId = skuId;
        this.check = check;
        this.title = title;
        this.image = image;
        this.skuAttr = skuAttr;
        this.price = price;
        this.count = count;
        this.totalPrice = totalPrice;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "skuId=" + skuId +
                ", check=" + check +
                ", title='" + title + '\'' +
                ", image='" + image + '\'' +
                ", skuAttr=" + skuAttr +
                ", price=" + price +
                ", count=" + count +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
