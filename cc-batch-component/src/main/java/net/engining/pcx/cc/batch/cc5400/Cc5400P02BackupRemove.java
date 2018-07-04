package net.engining.pcx.cc.batch.cc5400;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;

import net.engining.pcx.cc.infrastructure.shared.model.ApGlTxn;
import net.engining.pcx.cc.infrastructure.shared.model.ApGlTxnHst;

@Service
public class Cc5400P02BackupRemove implements ItemProcessor<ApGlTxn, ApGlTxn>{
	
	
	@PersistenceContext
	private EntityManager em;

	/*
	 * author fan.jiang
	 * 备份总账交易流水process
	 */

	public ApGlTxn process(ApGlTxn item) throws Exception {
		
		ApGlTxnHst apGltxnHis = new ApGlTxnHst();
		apGltxnHis.setAcctSeq(item.getAcctSeq());
		apGltxnHis.setAcqBranch(item.getAcqBranch());
		apGltxnHis.setAgeGroupCd(item.getAgeGroupCd());
		apGltxnHis.setCurrCd(item.getCurrCd());
		apGltxnHis.setGltSeq(item.getGltSeq());
		apGltxnHis.setOwingBranch(item.getOwingBranch());
		apGltxnHis.setPostAmount(item.getPostAmount());
		apGltxnHis.setPostCode(item.getPostCode());
		apGltxnHis.setPostDate(item.getPostDate());
		apGltxnHis.setPostDesc(item.getPostDesc());
		apGltxnHis.setPostGlInd(item.getPostGlInd());
		apGltxnHis.setTxnDetailSeq(item.getTxnDetailSeq());
		apGltxnHis.setTxnDetailType(item.getTxnDetailType());
		apGltxnHis.setTxnDirection(item.getTxnDirection());
		
		em.persist(apGltxnHis);

		em.remove(item);
			
		return item;
	}

}