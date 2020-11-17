package com.peng.repository;

import com.peng.bean.MatchBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

@Repository
public interface LiveDataNRepository extends JpaRepository<MatchBean, Serializable> {
    List<MatchBean> findAllByLiveDate(String date);

    MatchBean findFirstByOrderByLiveDateDesc();

    void deleteByLiveDateGreaterThanEqual(String date);

    MatchBean findFirstByMatchNumAndLiveDate(String matchNum, String liveDate);
}