package net.engining.pcx.cc.batch.cc1800;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

 
/**
 * @author daniel
 * 同步日终余额
 */
@Service
@Scope("step")
public class Cc1800P72SyncEndDayBeforeBal implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> { 
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) throws Exception {
		
		for (List<Cc1800IAccountInfo> cc1800IAccountInfos : item.getAccountList().values()) {
			
			for (Cc1800IAccountInfo cc1800IAccountInfo : cc1800IAccountInfos){
				for(CactSubAcct subAcct :cc1800IAccountInfo.getCactSubAccts()){
					if(subAcct.getEndDayBal().compareTo(subAcct.getEndDayBeforeBal())!=0 ){
						subAcct.setEndDayBeforeBal(subAcct.getEndDayBal());
					}
				}
			}
			
		 
		}
		
		return item;
	}
	
	 

}
