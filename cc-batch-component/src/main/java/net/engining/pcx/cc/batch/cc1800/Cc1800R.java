package net.engining.pcx.cc.batch.cc1800;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.QCactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.QCactAgeDue;
import net.engining.pcx.cc.infrastructure.shared.model.QCactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.QCactTxnPost;
import net.engining.pcx.cc.process.model.AcctModel;
import net.engining.pg.batch.sdk.AbstractKeyBasedStreamReader;

@Service
@StepScope
public class Cc1800R extends AbstractKeyBasedStreamReader<String, Cc1800IPostingInfo> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@PersistenceContext
	protected EntityManager em;
	
	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;

	@Override
	protected List<String> loadKeys() {
		QCactAccount qCactAccount = QCactAccount.cactAccount;

		return new JPAQueryFactory(em)
				.select(qCactAccount.custId)
				.from(qCactAccount)
				.where(qCactAccount.bizDate.loe(batchDate))
				.groupBy(qCactAccount.custId)
				.orderBy(qCactAccount.custId.asc())
				.fetch();
	}

	@Override
	protected Cc1800IPostingInfo loadItemByKey(String key) {
		Cc1800IPostingInfo cc1800IPostingInfo = new Cc1800IPostingInfo();
		cc1800IPostingInfo.setCustId(key);
		
		QCactAccount qCactAccount = QCactAccount.cactAccount;
		List<CactAccount> cactAccounts = new JPAQueryFactory(em)
				.select(qCactAccount)
				.from(qCactAccount)
				.where(
						qCactAccount.custId.eq(key)
						.and(qCactAccount.bizDate.loe(batchDate)))
				.orderBy(qCactAccount.acctNo.asc())
				.fetch();
		Map< Integer, List<Cc1800IAccountInfo>> accountList = new HashMap<Integer, List<Cc1800IAccountInfo>>();
		for(CactAccount cactAccount : cactAccounts){
			
			cc1800IPostingInfo.setOrg(cactAccount.getOrg());
			
			Cc1800IAccountInfo info = new Cc1800IAccountInfo();
			info.setCactAccount(cactAccount);
			//依次取子表
			Integer acctSeq = cactAccount.getAcctSeq();
			String currCd = cactAccount.getCurrCd();
			
			//子账户）
			QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
			info.setCactSubAccts(
					new JPAQueryFactory(em)
					.select(qCactSubAcct)
					.from(qCactSubAcct)
					.where(
							qCactSubAcct.acctSeq.eq(acctSeq)
							.and(qCactSubAcct.currCd.eq(currCd)))
					.fetch());
			
			//入账交易
			QCactTxnPost qCactTxnPost = QCactTxnPost.cactTxnPost;
			info.setCactTxnPosts(
					new JPAQueryFactory(em)
					.select(qCactTxnPost)
					.from(qCactTxnPost)
					.where(qCactTxnPost.acctSeq.eq(acctSeq)
							.and(qCactTxnPost.postCurrCd.eq(currCd))
							.and(qCactTxnPost.postDate.loe(batchDate)))
					.orderBy(qCactTxnPost.txnTime.asc())
					.fetch());
			
			//历史最小还款额
			QCactAgeDue qCactAgeDue = QCactAgeDue.cactAgeDue;
			info.setCactAgeDues(
					new JPAQueryFactory(em)
					.select(qCactAgeDue)
					.from(qCactAgeDue)
					.where(qCactAgeDue.acctSeq.eq(acctSeq))
					.orderBy(qCactAgeDue.graceDate.asc())
					.fetch());
			
			/*QCactCard qCactCard = QCactCard.cactCard;
			List<String> cardGroupId = new JPAQuery(em)
	                .from(qCactCard)
	                .where(qCactCard.acctNo.eq(cactAccount.getAcctNo()).and(qCactCard.businessType.eq(cactAccount.getBusinessType()))).groupBy(qCactCard.cardGroupId).list(qCactCard.cardGroupId);*/
			//卡组信息
			/*QCactCardGroup qCactCardGroup = QCactCardGroup.cactCardGroup;
			info.setCactCardGroups(new JPAQuery(em)
			        .from(qCactCardGroup)
			        .where(qCactCardGroup.cardGroupId.in(cardGroupId)).list(qCactCardGroup));*/
			
			//授权未达账
			/*List<AuthorizeInfo> authorizeInfos = new ArrayList<AuthorizeInfo>();
			QAuthUnmatch qAuthUnmatch = QAuthUnmatch.authUnmatch;
			List<AuthUnmatch> authUnmatchs = new JPAQuery(em)
			                                          .from(qAuthUnmatch)
			                                          .where(
					                                         qAuthUnmatch.acctNo.eq(acctSeq)
					                                         .and(qAuthUnmatch.chbCurrCd.eq(currCd))
					                                         .and(qAuthUnmatch.txnStatus.in(TxnStatusDef.A, TxnStatusDef.N))
					                                        )
			                                          .list(qAuthUnmatch);
			for (AuthUnmatch authUnmatch : authUnmatchs){
				AuthorizeInfo authorizeInfo = new AuthorizeInfo();
				authorizeInfo.setAuthUnmatch(authUnmatch);
				authorizeInfo.setUnmatchStatus(UnmatchStatus.U);
				authorizeInfos.add(authorizeInfo);
			}*/
//			info.setAuthorizeInfos(authorizeInfos);
			if (logger.isDebugEnabled()) {
				logger.debug("入账前数据收集：Org["+cactAccount.getOrg()
						+"],CurrCd["+cactAccount.getCurrCd()
						+"],AcctNo["+cactAccount.getAcctNo()
						+"],SubAccts.size["+info.getCactSubAccts().size()
						+"],TxnPosts.size["+info.getCactTxnPosts().size()
						+"]");
		    }
            //复制账户信息和信用计划信息
			copyAccountStatus(info);
            if(cc1800IPostingInfo.getAccountList().containsKey(cactAccount.getAcctNo())){
            	cc1800IPostingInfo.getAccountList().get(cactAccount.getAcctNo()).add(info);
            }
            else{
            	List<Cc1800IAccountInfo> infos = new ArrayList<Cc1800IAccountInfo>();
            	infos.add(info);
            	accountList.put(cactAccount.getAcctNo(), infos);
            	cc1800IPostingInfo.setAccountList(accountList);
            }
            
            //for new services
            AcctModel model = new AcctModel();
            model.setCactAccount(cactAccount);
            model.setCactSubAccts(info.getCactSubAccts());
            model.setCactAgeDues(info.getCactAgeDues());
            model.setCactTxnPosts(info.getCactTxnPosts());
            cc1800IPostingInfo.getAcctModelMap().put(cactAccount.getAcctSeq(), model);
            info.setAcctModel(model);		//用于重构过渡
            
		}
		return cc1800IPostingInfo;
	}
	
	/**
	 * 备份账户信息、信用计划信息
	 * 输出总账所需要交易流水时使用
	 * @param accountInfo
	 */
	private void copyAccountStatus(Cc1800IAccountInfo accountInfo){
		CactAccount preCactAccount = new CactAccount();
		preCactAccount.updateFromMap(accountInfo.getCactAccount().convertToMap());
		
		List<CactSubAcct> preCactSubAccts = new ArrayList<CactSubAcct>();
		for (CactSubAcct cactSubAcct : accountInfo.getCactSubAccts()){
			CactSubAcct preCactSubAcct = new CactSubAcct();
			preCactSubAcct.updateFromMap(cactSubAcct.convertToMap());
			preCactSubAccts.add(preCactSubAcct);
		}

		accountInfo.setPreCactSubAccts(preCactSubAccts);
		accountInfo.setPreCactAccount(preCactAccount);
	}

}
