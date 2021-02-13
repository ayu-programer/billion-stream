package com.roncoo.eshop.cache.model;

/**
 * 店铺信息
 * @author Administrator
 *
 */
public class ShopInfo {

	private Long shopId;
	private String name;
	private Integer level;
	private Double goodCommentRate;

	public Long getShopId() {
		return shopId;
	}

	public void setShopId(Long shopId) {
		this.shopId = shopId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Double getGoodCommentRate() {
		return goodCommentRate;
	}

	public void setGoodCommentRate(Double goodCommentRate) {
		this.goodCommentRate = goodCommentRate;
	}


	@Override
	public String toString() {
		return "ShopInfo{" +
				"shopId=" + shopId +
				", name='" + name + '\'' +
				", level=" + level +
				", goodCommentRate=" + goodCommentRate +
				'}';
	}
}
