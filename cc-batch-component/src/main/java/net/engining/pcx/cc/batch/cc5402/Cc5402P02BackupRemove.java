package net.engining.pcx.cc.batch.cc5402;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;

import net.engining.pcx.cc.infrastructure.shared.model.ApInternalGltxn;

@Service
public class Cc5402P02BackupRemove implements ItemProcessor<ApInternalGltxn, ApInternalGltxn>{
	
	
	@PersistenceContext
	private EntityManager em;

	/*
	 * author hu
	 * 备份内部户待入总账交易流水process
	 */

	public ApInternalGltxn process(ApInternalGltxn item) throws Exception {

		em.remove(item);
			
		return item;
	}

}