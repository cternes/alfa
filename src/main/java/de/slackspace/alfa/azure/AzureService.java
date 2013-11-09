package de.slackspace.alfa.azure;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Calendar;
import java.util.TimeZone;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.table.TableConfiguration;
import com.microsoft.windowsazure.services.table.TableContract;
import com.microsoft.windowsazure.services.table.TableService;
import com.microsoft.windowsazure.services.table.models.Filter;
import com.microsoft.windowsazure.services.table.models.QueryEntitiesOptions;
import com.microsoft.windowsazure.services.table.models.QueryEntitiesResult;

import de.slackspace.alfa.domain.TableResultPartial;

public class AzureService {

	private static final String WADLOGSTABLE = "WADLogsTable";
	private static final int MAX_DAYS_BACK = 10;
	private static AzureService INSTANCE;
	private TableContract tableContract;
	
	private AzureService(CloudStorageAccount account, TableContract contract) {
		this.tableContract = contract;
	}
	
	public static AzureService create(String accountName, String accountKey, String accountUrl) {
		if(INSTANCE == null) {
			try {
				CloudStorageAccount account = CloudStorageAccount.parse("DefaultEndpointsProtocol=https;AccountName="+ accountName +";AccountKey=" + accountKey);
				
				Configuration config = Configuration.getInstance();
				config.setProperty(TableConfiguration.ACCOUNT_NAME, accountName);
				config.setProperty(TableConfiguration.ACCOUNT_KEY, accountKey);
				config.setProperty(TableConfiguration.URI, accountUrl);
				TableContract contract = TableService.create(config);
				
				return new AzureService(account, contract);
			} catch (InvalidKeyException e) {
				throw new IllegalArgumentException("Could not create AzureService. Accountname or Accountkey incorrect.");
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException("Could not create AzureService. Accountname or Accountkey incorrect.");
			}
		}
		
		return INSTANCE;
	}
	
	public AzureService getInstance() {
		return INSTANCE;
	}
	
	public TableResultPartial getLogEntries(String nextPartitionKey, String nextRowKey) {
		try {
			QueryEntitiesOptions options = constructFilter(nextPartitionKey, nextRowKey);
			QueryEntitiesResult result = tableContract.queryEntities(WADLOGSTABLE, options);
			return new TableResultPartial(result.getEntities(), result.getNextPartitionKey(), result.getNextRowKey());
		} catch (ServiceException e) {
			throw new RuntimeException(e);
		}
	}
	
	private QueryEntitiesOptions constructFilter(String nextPartitionKey, String nextRowKey) {
		QueryEntitiesOptions options = new QueryEntitiesOptions();
		if(nextPartitionKey != null && nextRowKey != null) {
			options.setNextPartitionKey(nextPartitionKey);
			options.setNextRowKey(nextRowKey);
		}
		else {
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			cal.add(Calendar.DAY_OF_MONTH, MAX_DAYS_BACK * -1);
			String partitionKey = PartitionCalculator.calculatePartitionKeyFor(cal);
			options.setFilter(Filter.queryString("PartitionKey ge '" + partitionKey + "'"));
		}
		
		return options;
	}
	
}
