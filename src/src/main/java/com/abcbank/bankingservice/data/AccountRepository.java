package com.abcbank.bankingservice.data;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abcbank.bankingservice.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long>{

	/*
	 * @Query("max(id) FROM Account") long getMaxId();
	 */
	
}
