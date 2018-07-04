package net.engining.pcx.cc.batch.cc5100;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag4InternalAcct;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactInternalTxnPostHst;
import net.engining.pcx.cc.infrastructure.shared.model.CactIntrnlTxnPostBt;
import net.engining.pcx.cc.infrastructure.shared.model.CactIntrnlTxnPostSum;
import net.engining.pcx.cc.param.model.InternalAccount;
import net.engining.pcx.cc.param.model.Subject;
import net.engining.pcx.cc.param.model.enums.InternalAccountStatus;
import net.engining.pg.parameter.OrganizationContextHolder;
import net.engining.pg.parameter.ParameterFacility;

/**
 * 将批量时的内部入账流水表的记录，分片分组插入内部账户汇总表
 */
public class Cc5100W  implements ItemWriter<CactIntrnlTxnPostBt> {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@PersistenceContext
	private EntityManager em;

	@Autowired
	private ParameterFacility parameterFacility;
	
	@Override
	public void write(List<? extends CactIntrnlTxnPostBt> items) throws Exception {
		
		
		Map<String ,List<CactIntrnlTxnPostBt>>  groups = new HashMap<String, List<CactIntrnlTxnPostBt>>();
		
		if(items.size()>0){
			for(CactIntrnlTxnPostBt inPost4Batch :items ){
				OrganizationContextHolder.setCurrentOrganizationId(inPost4Batch.getOrg());
				if(groups.get(inPost4Batch.getInternalAcctId()) != null){
					
					groups.get(inPost4Batch.getInternalAcctId()).add(inPost4Batch);
					
				}else {
					
					List<CactIntrnlTxnPostBt> interList = new ArrayList<CactIntrnlTxnPostBt>();
					
					interList.add(inPost4Batch);
					groups.put(inPost4Batch.getInternalAcctId(), interList);
				}
				
			}
			
			if(groups.size()>0){
				for(String internalAcctId : groups.keySet()){
					
					CactIntrnlTxnPostSum txnPostSummar = new CactIntrnlTxnPostSum();
					txnPostSummar.setInternalAcctId(internalAcctId);
					txnPostSummar.setCrAmt(BigDecimal.ZERO);
					txnPostSummar.setDbAmt(BigDecimal.ZERO);
					
					List<CactIntrnlTxnPostBt> internalPostList = groups.get(internalAcctId);
					for(CactIntrnlTxnPostBt detail :internalPostList){
						
						logger.debug("detail.getInternalAcctId()为{}",detail.getInternalAcctId());
						//查内部账户对应参数
						InternalAccount internalAccount = parameterFacility.loadParameter(InternalAccount.class, detail.getInternalAcctId());
						
						Subject subject = parameterFacility.loadParameter(Subject.class, internalAccount.subjectCd);

						//内部账户状态必须是open才能入账
						if(internalAccount.status.equals(InternalAccountStatus.OPEN)){
							 
							internalTxnPostUpdateAmt(subject ,detail ,txnPostSummar);
							detail.setPostingFlag(PostingFlag4InternalAcct.F00);
							
						}else if(internalAccount.status.equals(InternalAccountStatus.CLOSE)){
							
							detail.setPostingFlag(PostingFlag4InternalAcct.F01);
							
						}else if(internalAccount.status.equals(InternalAccountStatus.DESTROY)){
							
							detail.setPostingFlag(PostingFlag4InternalAcct.F02);
							
						}
						em.persist(txnPostSummar);
						CactInternalTxnPostHst  hst = new CactInternalTxnPostHst();
						hst.setDbCrInd(detail.getDbCrInd());
						hst.setInternalAcctId(detail.getInternalAcctId());
						hst.setInternalAcctPostCode(detail.getInternalAcctPostCode());
						hst.setOrg(detail.getOrg());
						hst.setPostAmt(detail.getPostAmt());
						hst.setPostCurrCd(detail.getPostCurrCd());
						hst.setPostDate(detail.getPostDate());
						hst.setDbCrInd(detail.getDbCrInd());
						hst.setRedBlueInd(detail.getRedBlueInd());
						hst.setPostingFlag(detail.getPostingFlag());
						hst.setTxnPostSeq(detail.getTxnSeq());
						hst.setTxnPostType(TxnDetailType.I);
						hst.setLastUpdateDate(new Date());
						hst.setTxnDetailSeq(detail.getTxnDetailSeq());
						hst.setTxnDetailType(detail.getTxnDetailType());
						em.persist(hst);
						em.remove(detail);
					}
					
				}
			}
		}
	}
	
	
	public void internalTxnPostUpdateAmt(Subject subject , CactIntrnlTxnPostBt detail  , CactIntrnlTxnPostSum txnPostSummar){
		
		switch (detail.getRedBlueInd()) {
		case R: // 红字(撤销交易)
			 writeOff(subject, detail, txnPostSummar);
			break;
		case N: // 正常交易
			 writeOn(subject, detail, txnPostSummar);
			break;
		case B: // 蓝字（正常交易）
			writeOn(subject,  detail, txnPostSummar);
			break;
		default:
			throw new RuntimeException("枚举未定义");
		}
		
	}

	public void writeOn(Subject subject , CactIntrnlTxnPostBt detail  , CactIntrnlTxnPostSum txnPostSummar){
		// 计算余额,发生额
		switch (subject.balDbCrFlag) {
		case D: // 只允许借方余额
			switch (detail.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额+借方金额
				txnPostSummar.setDbAmt(txnPostSummar.getDbAmt().add(detail.getPostAmt()));
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额+贷方金额
				txnPostSummar.setCrAmt(txnPostSummar.getCrAmt().add(detail.getPostAmt()));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}
	
			break;
		case C: // 只允许贷方余额
			switch (detail.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额+借方金额
				txnPostSummar.setDbAmt(txnPostSummar.getDbAmt().add(detail.getPostAmt()));
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额+贷方金额
				txnPostSummar.setCrAmt(txnPostSummar.getCrAmt().add(detail.getPostAmt()));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}
	
			break;
		case B: // 按轧差金额
			// TODO 按轧差金额计算需要把一方置0
	
			switch (detail.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额+借方金额
				txnPostSummar.setDbAmt(txnPostSummar.getDbAmt().add(detail.getPostAmt()));
			 
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额+贷方金额
				txnPostSummar.setCrAmt(txnPostSummar.getCrAmt().add(detail.getPostAmt()));
				 
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}
	
			break;
		case T: // 双向余额
			switch (detail.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额+借方金额
				txnPostSummar.setDbAmt(txnPostSummar.getDbAmt().add(detail.getPostAmt()));
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额+贷方金额
				txnPostSummar.setCrAmt(txnPostSummar.getCrAmt().add(detail.getPostAmt()));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}
	
			break;
		default:
			break;
		}
			
	}


	public void writeOff(Subject subject , CactIntrnlTxnPostBt detail  , CactIntrnlTxnPostSum txnPostSummar) {
		 
		// 统计余额,发生额
		switch (subject.balDbCrFlag) {
		case D: // 只允许借方余额
			switch (detail.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额-借方金额
				txnPostSummar.setDbAmt(txnPostSummar.getDbAmt().subtract(detail.getPostAmt()));
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额-贷方金额
				txnPostSummar.setCrAmt(txnPostSummar.getCrAmt().subtract(detail.getPostAmt()));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case C: // 只允许贷方余额
			switch (detail.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额-借方金额
				txnPostSummar.setDbAmt(txnPostSummar.getDbAmt().subtract(detail.getPostAmt()));
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额-贷方金额
				txnPostSummar.setCrAmt(txnPostSummar.getCrAmt().subtract(detail.getPostAmt()));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case B: // 按轧差金额
			// TODO 按轧差金额计算需要把一方置0

			switch (detail.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额-借方金额
				txnPostSummar.setDbAmt(txnPostSummar.getDbAmt().subtract(detail.getPostAmt()));
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额-贷方金额
				txnPostSummar.setCrAmt(txnPostSummar.getCrAmt().subtract(detail.getPostAmt()));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case T: // 双向余额
			switch (detail.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额-借方金额
				txnPostSummar.setDbAmt(txnPostSummar.getDbAmt().subtract(detail.getPostAmt()));
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额-贷方金额
				txnPostSummar.setCrAmt(txnPostSummar.getCrAmt().subtract(detail.getPostAmt()));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		default:
			break;
		}
		 
	}

}
