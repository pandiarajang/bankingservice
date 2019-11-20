package com.abcbank.bankingservice.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.abcbank.bankingservice.data.AccountRepository;
import com.abcbank.bankingservice.data.CustAccMappingRepository;
import com.abcbank.bankingservice.data.CustomerRepository;
import com.abcbank.bankingservice.data.IDGenerator;
import com.abcbank.bankingservice.model.Account;
import com.abcbank.bankingservice.model.Customer;
import com.abcbank.bankingservice.model.CustomerAccount;

@Component
public class ServiceImpl {

	@Autowired
	private AccountRepository accRepo;
	
	@Autowired
	private CustAccMappingRepository mapRepo;
	
	@Autowired
	private CustomerRepository custRepo;
	
	
	
	public List<Account> getAllAccounts() {
		
		Map<Long, Account> accounts=new HashMap<>();		
		List<CustomerAccount> map=mapRepo.findAll();
		
		for(CustomerAccount custAcc:map) {
			long accId=custAcc.getAccountId();
			long cusId=custAcc.getCustomerId();
			 Account account=null;
			 Customer customer=null;
			 
			 if(!accounts.containsKey(accId)){
				 account=accRepo.findById(accId).get();
				 accounts.put(accId, account);
			 }else {
				 account=accounts.get(accId);
			 }
			 if(!account.hasCustomer(cusId)) {
				 customer=custRepo.findById(cusId).get();
				 account.addCustomer(customer);
			 }
			 
		}
		List<Account> accountList= new ArrayList<Account>(accounts.values());
		return accountList;
	}

	
	public Account getAccount(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Customer getAllCustomers() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Customer getCustomerInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getInfo() {
		return "Welcome to ABC Bank Limited \n"
				+ "Follow the instructions for new Account Management and Fund Transfer \n";
	}

	/***
	 * Steps
	 * -----
	 * 1) get all customers from Customer table
	 * 2) get account id for each customer by iterating over customer account mapping table
	 * 3) get account object from account table and add it to customer object
	 * 
	 * 
	 */
	public List<Customer> getCustomers(String[] fstNames,String[] mdlNames,String[] lstNames) {
		// TODO Auto-generated method stub
		List<Customer> customers=new ArrayList<Customer>();
		int idx=0;
		for(String fstName:fstNames) {			
			List<Customer> custs=custRepo.findByName(fstName, mdlNames[idx], lstNames[idx]);
			for(Customer cust:custs) {
				List<CustomerAccount> maps=mapRepo.getCustomerAccountByCustomerId(cust.getId());
				for(CustomerAccount map:maps) {
					Account account=accRepo.findById(map.getAccountId()).get();
					cust.setAccount(account);
				}
				customers.add(cust);
			}			
			idx++;			
		}		
		return customers;
	}

	/****
	 * Logic 1
	 * 1)  Check if account data is available
	 * 2)  Store account data in Account mapping and Account table
	 * 3)  If account data not available ignore
	 * 4)  Assign ids accordingly to Account, Customer and CustomerAccount data.
	 * 
	 * Test Cases : Nil
	 * 
	 * @param customer
	 */
	public Customer addCustomer(Customer customer) {
		/*
		 * Logic 1
		 * Mockito-BTest01 
		 * General Test logic:
		 *  1) Pass customer object with Account data and 
		 *  check accRepo.save is invoked only once
		 *  Mockito-BTest02
		 *  2) Pass customer object without Account data and 
		 *  check accRepo.save is never invoked  
		 *  Mockito-BTest03
		 *  4)  Check ids are assigned to customer object accordingly. 
		 */		
		long custId;
		
		if(isCustomerExists(customer)) {
			List<Customer> customers2=custRepo.findByName(
					customer.getFirstName(), customer.getMiddleName(), customer.getLastName());			
			custId=customers2.get(0).getId();
		}else {
			custId=IDGenerator.getCustId();
		}
		
		
		if (customer.getAccount()!=null) {
			Account acc=customer.getAccount();	
			long accId=IDGenerator.getAccId();
			long mapId=IDGenerator.getMapId();			
			acc.setId(accId);
			accRepo.save(acc);			
			CustomerAccount map=new CustomerAccount(mapId, custId, accId);
			mapRepo.save(map);			
		}			
		
		customer.setId(custId);
		custRepo.save(customer);
		
		
		return customer;
	}
	
	/***
	 * Add Account
	 * 
	 * Account must have at-least one customer.
	 * Account can have more than one customer.
	 * Account without any customer associated must not be saved in the DB.
	 * Assign ID to Account, Customer data and save. 
	 * If customer data already available use existing customer data. 
	 * 
	 * Test Cases :
	 * 1) Mockito-BTest04, Pass null object and expect null as a return value. 
	 * 2) Mockito-BTest05, If no customers assigned, Expect null as a return value and verify none of the repo.save methods are called. 
	 * 3) Mockito-BTest06, Pass one customer and verify custRepo.save is called only once.
	 * 4) Mockito-BTest07, Pass more than one customer and verify custRepo.save is called as many times as many customers assigned.
	 * 5) Mockito-BTest08, pass account object with one existing customer and verify 
	 * 			a) custRepo.save is never called 
	 * 			b) accRepo.save is called only once
	 * 			c) mapRepo.save is called only once 
	 * 			d) ids are assigned accordingly for map and acc data
	 * 
	 * 
	 */
	
	public Account addAccount(Account account) {
		
		/*
		 * Logic 1
		 *
		 * Test : Mockito-BTest04
		 */
		
		if(account==null) {
			//accRepo.save(new Account());  // Uncomment this line to fail Mockito-BTest04.
			return null;
		}
		
		

		/*
		 * Logic 2
		 * Process save account data and customer data if available, Without customer details don't store account data.
		 * 
		 * Tests :
		 * 2) Mockito-BTest05, If no customers assigned, Expect null as a return value and verify none of the repo.save methods are called. 
		 * 3) Mockito-BTest06, Pass one customer and verify custRepo.save is called only once.
		 * 4) Mockito-BTest07, Pass more than one customer and verify custRepo.save is called as many times as many customers assigned.
		 * 5) Mockito-BTest08, pass account object with one existing customer and verify 
		 * 			a) custRepo.save is never called 
		 * 			b) accRepo.save is called only once
		 * 			c) mapRepo.save is called only once 
		 * 			d) ids are assigned accordingly for map and acc data
		 */
		
		if(!account.getCustomers().isEmpty()) {
			// save account data
			long accId=IDGenerator.getAccId();
			account.setId(accId);
			accRepo.save(account);
			List<Customer> customers=account.getCustomers();
			for(Customer customer:customers) {
				long custId;
				if(isCustomerExists(customer)) {
					List<Customer> customers2=custRepo.findByName(
							customer.getFirstName(), customer.getMiddleName(), customer.getLastName());
					
					custId=customers2.get(0).getId();
					customer.setId(custId);
				}else {
					custId=this.addCustomer(customer).getId();
				}
				mapRepo.save(new CustomerAccount(IDGenerator.getMapId(), custId, accId));				
				//break; // Uncomment this line to fail Mockito-BTest05.
			}
		}else {
			//accRepo.save(new Account());  // Uncomment this line to fail Mockito-BTest05.
			return null;
		}
		
		return account;  // return null to fail Mockito-BTest06 and Mockito-BTest07
	}
	
	/***
	 * Logic
	 * 1) Check transferee account is valid and active
	 * 3) Check beneficiary account is valid and active
	 * 4) Check customer has sufficient fund, if not return false
	 * 5) perform transaction to deduct money from transferee account and credit money to beneficiary account
	 * Tests
	 * 1) Mockito-BTest09
	 * 2) Mockito-BTest10
	 * 3) Mockito-BTest11
	 * 4) Mockito-BTest12
	 * 
	 */
	@Transactional
	public boolean transferFund(long trAccId,long bfcAccId,long fund) {		
		Optional<Account> trAccO=accRepo.findById(trAccId);
		Optional<Account> bfcAccO=accRepo.findById(bfcAccId);
		
		
		/**
		 * Logic 1 
		 * Check Transferee account is valid and Active
		 * 
		 * TEST
		 * Mockito-BTest09 
		 * pass non-existing account id and check for boolean response
		 * Mockito-BTest10
		 * pass id of inactive account and check for boolean response
		 */
		if(trAccO==null) {
		    return false;		
		}
		else if(!trAccO.isPresent()) {
			return false; 
		}else if(!trAccO.get().getStatus().equalsIgnoreCase("active")) {
			return false;
		}
		
		/**
		 * Logic 2
		 * Check Beneficiary account is valid and Active
		 * TEST
		 * Mockito-BTest11
		 * pass non existing account id and check for boolean response
		 * Mockito-BTest12
		 * pass id of inactive account and check for boolean response
		 * 
		 */
		if(bfcAccO==null) {
		    return false;		
		}
		else if(!bfcAccO.isPresent()) {
			return false; 
		}else if(!bfcAccO.get().getStatus().equalsIgnoreCase("active")) {
			return false;
		}
		
		/**
		 * Logic 2
		 * Check Transferee has sufficient fund
	 	 * TEST
		 * Mockito-BTest13
		 * Pass account id--which has zero balance--and check for boolean response
		 * Mockito-BTest14
		 * Pass account id--which has sufficient balance--and check for boolean response
		 */
		Account trAcc=trAccO.get();
		Account bfcAcc=bfcAccO.get();
		
		if(trAcc.getBalance()<fund) {
			return false;
		}		
		
		/**
		 * Logic 3
		 * Transaction 
		 *  1) debit fund from transferee account
		 *  2) credit fund to beneficiary account
		 * TEST  
		 * Mockito-BTest15 
		 * Pass valid Transferee account and check fund is debited from Transferee and credited to beneficiary.
		 * 
		 * Note: Transaction cannot be tested in this layer.
		 */
		
		trAcc.setBalance(trAcc.getBalance()-fund);
		accRepo.save(trAcc);
		bfcAcc.setBalance(bfcAcc.getBalance()+fund);
		accRepo.save(bfcAcc);
		return true;
	}
	
	/**
	 * Modify customer, Check if customer object is available if yes modify and return true.
	 * 
	 * TEST
	 *  Mockito-BTest16
	 *  Pass non-existing customer and check for boolean response
	 *  
	 *  Mockito-BTest17
	 *  Pass existing customer and check for boolean response
	 *  
	 */
	
	public boolean modifyCustomer(Customer customer) {
		//custRepo.save(customer); //uncomment this line to fail Mockito-BTest16 and Mockito-BTest17
		if(isCustomerExists(customer)) {
			addCustomer(customer);
			return true;
		}
		return false;
		
	}
	
	/***
	 * Business Service Utils
	 * 
	 */
	/**
	 * Check if customer data with same name available in database. 
	 * 
	 * Test cases: Nil
	 */
	public boolean isCustomerExists(Customer customer) {
		
		if(!custRepo.findByName(customer.getFirstName(), customer.getMiddleName(), customer.getLastName()).isEmpty()) {
			return true;
		}
		return false;
	}
	

}
