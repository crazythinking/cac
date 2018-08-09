package net.engining.pcx.cc.batch.cc1800;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;

import net.engining.pg.parameter.OrganizationContextHolder;


/**
 * 为下面的所有Processor设置Organization上下文
 * @author licj
 *
 */
@Service
@StepScope
public class Cc1800P00OrganizationSetup implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {

	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) throws Exception
	{
		OrganizationContextHolder.setCurrentOrganizationId(item.getOrg());
		
		return item;
	}
	
}
