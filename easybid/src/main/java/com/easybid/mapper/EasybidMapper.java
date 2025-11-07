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

	EasybidItem findLatestByPlnmNo(Long plnmNo);

	List<EasybidItem> findPagedAll(@Param("offset") int offset,@Param("numOfRows") int numOfRows);

	int getTotalCount();

	EasybidItem getDetails(@Param("id") Long id);

	EasybidItem findUuid(@Param("uuid") String uuid);

}
