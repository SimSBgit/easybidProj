package com.easybid.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.easybid.model.EasybidItem;

@Mapper
public interface EasybidMapper {

	void insert(EasybidItem item);

	List<EasybidItem> findAll();

	EasybidItem findByPlnmNoAndPbctNo(@Param("plnmNo") Long plnmNo, 
									  @Param("pbctNo") Long pbctNo);

}
