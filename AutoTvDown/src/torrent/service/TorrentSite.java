package torrent.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import torrent.dao.TorrentManageDao;

public class TorrentSite
{

	private String cookieUrl;
	private String searchUrl;
	private String downUrl;
	private String downDate;
	
	private StringBuffer cookie = new StringBuffer();
	
	private TorrentManageDao manageDao = new TorrentManageDao();
	
	public TorrentSite()
	{
		new TorrentSite("http://naver.com","http://naver.com","http://naver.com");
	}
	
	public TorrentSite(String cookieUrl, String searchUrl, String downUrl)
	{
		this.cookieUrl = cookieUrl;
		this.searchUrl = searchUrl;
		this.downUrl = downUrl;		
	}
	
	public void todayContentsInit()
	{
		int cnt = manageDao.selectConfirmTarget();
		
		if (cnt > 0)
		{
			manageDao.insertDownLoadTarget();
		}		
	}
	
	public void getSiteCookie() throws Exception
	{
		URL url = new URL(cookieUrl);
		URLConnection urlConn = url.openConnection();
		urlConn.setRequestProperty("User-Agent", "Mozilla/4.0");

		Map map = urlConn.getHeaderFields();

		if(map.containsKey("Set-Cookie"))
		{
		        Collection c =(Collection)map.get("Set-Cookie");
		
		        for(Iterator i = c.iterator(); i.hasNext(); )
		        {
		        	cookie.append(i.next());
		        }
		}
	}
	
	public void getSiteSearchRslt() throws Exception
	{
		URL url;
		URLConnection urlConn;
		InputStream is;
		InputStreamReader isr;
		BufferedReader br;
		String str;
		StringBuffer searchStr;
		HashMap<String, String> hm;
		
		List contentsList = manageDao.selectNotDownloadList();
	
		
		for (Object ob:contentsList)
		{
			 hm = (HashMap<String, String>)ob;
			searchStr = new StringBuffer();
			
			url = new URL(searchUrl+hm.get("name"));
			urlConn = url.openConnection();
			
			is = urlConn.getInputStream();
			isr = new InputStreamReader(is, "utf-8");
			br = new BufferedReader(isr);
 
			
			while((str = br.readLine()) != null)
			{
				searchStr.append(str + "\r\n");
			}
			
			
			getTorrentFile(getContentsList(searchStr));
			
			br.close();
			isr.close();
			is.close();
		}
	}
	
	public String getContentsList(StringBuffer searchStr)
	{
		SimpleDateFormat sd = new SimpleDateFormat("yyMMdd");
		downDate = sd.format(new Date());
		
		String findStr = "";
		
		String makeRepExp = "<a href='s.php.+>\r?\n+.+\r?\n+.+"+downDate+".+720.+>";
		//<a href='./board.php\?bo_table=[\w]{1,}&wr_id=[\d]{1,}'>.+?150306.+?720.+?</a>
		
		Pattern p = Pattern.compile(makeRepExp);
		Matcher m = p.matcher(searchStr);
				
		if (m.find())
		{
			findStr = m.group();				
		}
		
		String reg ="bo_table=.+&wr_id=[\\d]*";
		
		p = Pattern.compile(reg);
		m = p.matcher(findStr);
		
		if (m.find())
		{
			findStr = m.group();
		}
		
		return findStr;
	}
	
	public void getTorrentFile(String findStr) throws Exception
	{
		URL url = new URL(downUrl+findStr);
		
		URLConnection downConn = url.openConnection();
		downConn.setRequestProperty("User-Agent", "Mozilla/4.0");
		downConn.setRequestProperty("Cookie", cookie.toString());
		downConn.setRequestProperty("Referer", "http://torrentman.net/bbs/bbc.php?"+findStr);
		
		InputStream is;
		InputStreamReader isr;
		BufferedReader br;
		String str;
		
		is = downConn.getInputStream();
		
		File file = new File("D:\\Study\\2014_workspace\\AutoTvDown\\src\\abc.torrent");
		FileOutputStream out = new FileOutputStream(file);
		int ch;		
		
		while((ch = is.read()) != -1)
		{
			out.write(ch);
		}
	}
	
}
