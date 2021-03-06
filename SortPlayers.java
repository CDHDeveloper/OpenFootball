import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class SortPlayers extends HttpServlet
   {
   private ConnectionPool pool;
   private ServletContext context;
   private static String servletName="SortPlayers";

   public void init()
      {
      context = getServletContext();
      synchronized(context)
        {
        pool=(ConnectionPool)context.getAttribute("pool");
        if(pool==null)
          {
          String driverClassName = context.getInitParameter("driverClassName");
          String url = context.getInitParameter("url");
          String userName = context.getInitParameter("username");
          String password = context.getInitParameter("password");
          try
            {
            pool=new ConnectionPool(driverClassName,url,userName,password);
            }
          catch(Exception error)
            {
			Routines.writeToLog(servletName,"Unable to create connection pool : " + error,false,context);
            }
          context.setAttribute("pool",pool);
          }
        }
      }

   public void doPost(HttpServletRequest request,
                      HttpServletResponse response)
      {
      doGet(request,response);
      }

   public void doGet(HttpServletRequest request,
                     HttpServletResponse response)
      {
      response.setContentType("text/html");
	  PrintWriter webPageOutput=null;
	  try
		{
		webPageOutput=response.getWriter();
		}
	  catch(IOException error)
		{
		Routines.writeToLog(servletName,"Error getting writer : " + error,false,context);
		}
      HttpSession session=request.getSession();
      int positionNumber=Routines.safeParseInt(request.getParameter("positionNumber"));
      int currentPositionNumber=Routines.safeParseInt(request.getParameter("currentPositionNumber"));
      int leagueNumber=Routines.safeParseInt(request.getParameter("league"));
      int teamNumber=Routines.safeParseInt(request.getParameter("team"));
      int currentPage=Routines.safeParseInt(request.getParameter("currentPage"));
      int currentSkillsButton=Routines.safeParseInt(request.getParameter("currentSkillButton"));
      int sortPage=Routines.safeParseInt(request.getParameter("sortPage"));
      int sortSkill=Routines.safeParseInt(request.getParameter("sortSkill"));
      String pageText=request.getParameter("page");
      String skillsText=request.getParameter("skills");
      String skill1=request.getParameter("skill1");
      String skill2=request.getParameter("skill2");
      String skill3=request.getParameter("skill3");
      String skill4=request.getParameter("skill4");
      String skill5=request.getParameter("skill5");
      if(sortPage==0||sortSkill==0)
        {
        if(positionNumber==0)
          {
          sortPage=1;
          sortSkill=1;
          skill1="skill1";
          }
        else
          {
          sortPage=1;
          sortSkill=1;
          skill1="skill1";
          }
        }
      if(currentPositionNumber!=positionNumber)
        {
        currentPage=0;
        currentSkillsButton=1;
        pageText="";
        skillsText="";
        sortPage=1;
        if(positionNumber==0)
          {
          sortSkill=1;
          skill1="skill1";
          }
        else
          {
          sortSkill=1;
          skill1="skill1";
          }
        }
      if(currentSkillsButton==0)
        {
        currentSkillsButton=1;
        }
      int page=0;
      if("Overall".equals(skillsText))
        {
        currentSkillsButton=1;
        }
      if("Skills1".equals(skillsText))
        {
        currentSkillsButton=2;
        }
      if("Skills2".equals(skillsText))
        {
        currentSkillsButton=3;
        }
      if("Skills3".equals(skillsText))
        {
        currentSkillsButton=4;
        }
      if("Page1".equals(pageText)||(pageText==null&&currentPage==1))
        {
        page=1;
        }
      if("Page2".equals(pageText)||(pageText==null&&currentPage==2))
        {
        page=2;
        }
      if("Page3".equals(pageText)||(pageText==null&&currentPage==3))
        {
        page=3;
        }
      if("Page4".equals(pageText)||(pageText==null&&currentPage==4))
        {
        page=4;
        }
      if("Page5".equals(pageText)||(pageText==null&&currentPage==5))
        {
        page=5;
        }
      if("Page6".equals(pageText)||(pageText==null&&currentPage==6))
        {
        page=6;
        }
      if("Page7".equals(pageText)||(pageText==null&&currentPage==7))
        {
        page=7;
        }
      if("Page8".equals(pageText)||(pageText==null&&currentPage==8))
        {
        page=8;
        }
      if(skill1!=null)
        {
        sortPage=currentSkillsButton;
        sortSkill=1;
        }
      if(skill2!=null)
        {
        sortPage=currentSkillsButton;
        sortSkill=2;
        }
      if(skill3!=null)
        {
        sortPage=currentSkillsButton;
        sortSkill=3;
        }
      if(skill4!=null)
        {
        sortPage=currentSkillsButton;
        sortSkill=4;
        }
      if(skill5!=null)
        {
        sortPage=currentSkillsButton;
        sortSkill=5;
        }
      if(sortPage==0)
        {
        sortPage=1;
        }
      String positionName="";
      String action=request.getParameter("action");
      String[] positions=null;
      int[] positionNumbers=null;
      if(session.isNew()&&
        ("Page1".equals(pageText)||
         "Page2".equals(pageText)||
         "Page3".equals(pageText)||
         "Page4".equals(pageText)||
         "Page5".equals(pageText)||
         "Page6".equals(pageText)||
         "Page7".equals(pageText)||
         "Page8".equals(pageText)))
        {
        session.setAttribute("redirect",
                             "http://" +
                             request.getServerName() +
                             ":" +
                             request.getServerPort() +
                             request.getContextPath() +
                             "/servlet/SortPlayers?jsessionid=" + session.getId() + "&league=" + leagueNumber + "&team=" + teamNumber);
        }
      else
        {
        session.setAttribute("redirect",request.getRequestURL() + "?" + request.getQueryString());
        }
      Connection database=null;
      try
        {
        database=pool.getConnection(servletName);
        }
      catch(SQLException error)
        {
		Routines.writeToLog(servletName,"Unable to connect to database : " + error,false,context);	
        }
      if(Routines.loginCheck(false,request,response,database,context))
        {
        return;
        }
      try
        {
        Statement sql=database.createStatement();
        ResultSet queryResult;
        queryResult=sql.executeQuery("SELECT COUNT(PositionNumber) " +
                                     "FROM positions " +
                                     "WHERE RealPosition=1 " +
                                     "AND Type!=3");
        int numOfPositions=0;
        if(queryResult.first())
          {
          numOfPositions=queryResult.getInt(1)+1;
          }
        positions=new String[numOfPositions];
        positionNumbers=new int[numOfPositions];
        positions[0]="All Positions";
        queryResult=sql.executeQuery("SELECT PositionNumber,PositionName " +
                                     "FROM positions " +
                                     "WHERE RealPosition=1 " +
                                     "AND Type!=3 " +
                                     "ORDER BY Type ASC, Sequence ASC");
        int currentPosition=0;
        while(queryResult.next())
          {
          positionNumbers[currentPosition+1]=queryResult.getInt(1);
          positions[currentPosition+1]=queryResult.getString(2);
          currentPosition++;
          }
        }
      catch(SQLException error)
        {
		Routines.writeToLog(servletName,"Unable to get Positions : " + error,false,context);		
        }
      Routines.WriteHTMLHead("View Players",//title
                             true,//showMenu
                             9,//menuHighLight
                             false,//seasonsMenu
		                     false,//weeksMenu
                             false,//scores
                             false,//standings
                             false,//gameCenter
                             false,//schedules
                             false,//previews
                             false,//teamCenter
		                     false,//draft
                             database,//database
                             request,//request
                             response,//response
                             webPageOutput,//webPageOutput
                             context);//context
      webPageOutput.println("<CENTER>");
      webPageOutput.println("<IMG SRC=\"../Images/ViewPlayers.jpg\"" +
                            " WIDTH='495' HEIGHT='79' ALT='View Players'>");
      webPageOutput.println(Routines.spaceLines(1));
      webPageOutput.println("<IMG SRC=\"../Images/Boss.gif\"" +
                            " WIDTH='160' HEIGHT='120' ALT='Boss'>");
      webPageOutput.println("</CENTER>");
      boolean[] returnBool=Routines.playerDraft(leagueNumber,teamNumber,session,database);
      boolean lockDown=returnBool[0];
      boolean playerDraft=returnBool[1];
      if(!playerDraft||lockDown||"Return to MyTeam page".equals(action))
        {
        if(!playerDraft||lockDown)
          {
          session.setAttribute("message","Draft deadline has passed, the draft will commence shortly");
          }
        session.setAttribute("redirect",
                             "http://" +
                             request.getServerName() +
                             ":" +
                             request.getServerPort() +
                             request.getContextPath() +
                             "/servlet/MyTeam?jsessionid=" + session.getId() + "&league=" + leagueNumber + "&team=" + teamNumber);
        try
          {
          response.sendRedirect((String)session.getAttribute("redirect"));
          }
        catch(IOException error)
          {
		  Routines.writeToLog(servletName,"Unable to redirect : " + error,false,context);	
          }	
        return;  
        }
      if((String)session.getAttribute("message")!=null)
        {
        Routines.tableStart(false,webPageOutput);
        Routines.tableHeader("Messages",0,webPageOutput);
        Routines.tableDataStart(true,false,true,true,false,0,0,"scoresrow",webPageOutput);
        Routines.messageCheck(false,request,webPageOutput);
        Routines.tableDataEnd(true,false,true,webPageOutput);
        Routines.tableEnd(webPageOutput);
        webPageOutput.println(Routines.spaceLines(1));
        }
      webPageOutput.println("<FORM ACTION=\"http://" +
                             request.getServerName() +
                             ":" +
                             request.getServerPort() +
                             request.getContextPath() +
                             "/servlet/SortPlayers\" METHOD=\"POST\">");
      webPageOutput.println("<CENTER>");
      boolean disabledLink1=false;
      boolean disabledLink2=false;
      boolean disabledLink3=false;
      boolean disabledLink4=false;
      boolean disabledLink5=false;
      boolean disabledLink6=false;
      boolean disabledLink7=false;
      boolean disabledLink8=false;
      if(page==0)
        {
        page++;
        }
      if(page==1)
        {
        disabledLink1=true;
        }
      if(page==2)
        {
        disabledLink2=true;
        }
      if(page==3)
        {
        disabledLink3=true;
        }
      if(page==4)
        {
        disabledLink4=true;
        }
      if(page==5)
        {
        disabledLink5=true;
        }
      if(page==6)
        {
        disabledLink6=true;
        }
      if(page==7)
        {
        disabledLink7=true;
        }
      if(page==8)
        {
        disabledLink8=true;
        }
      currentPage=page;
      if(disabledLink1)
        {
        webPageOutput.println("Page 1");
        }
      else
        {
        Routines.WriteHTMLLink(request,
                               response,
                               webPageOutput,
                               "SortPlayers",
                               "page=Page1"+
                               "&league=" + leagueNumber +
                               "&team=" + teamNumber +
                               "&currentPage=" + currentPage +
                               "&currentSkillButton=" + currentSkillsButton +
                               "&positionNumber=" + positionNumber +
                               "&currentPositionNumber=" + positionNumber +
                               "&sortPage=" + sortPage +
                               "&sortSkill=" + sortSkill,
                               "Page 1",
                               "opt",
                               true);
        }
      webPageOutput.println("<B>�</B>");
      if(disabledLink2)
        {
        webPageOutput.println("Page 2");
        }
      else
        {
        Routines.WriteHTMLLink(request,
                               response,
                               webPageOutput,
                               "SortPlayers",
                               "page=Page2"+
                               "&league=" + leagueNumber +
                               "&team=" + teamNumber +
                               "&currentPage=" + currentPage +
                               "&currentSkillButton=" + currentSkillsButton +
                               "&positionNumber=" + positionNumber +
                               "&currentPositionNumber=" + positionNumber +
                               "&sortPage=" + sortPage +
                               "&sortSkill=" + sortSkill,
                               "Page 2",
                               "opt",
                               true);
        }
      webPageOutput.println("<B>�</B>");
      if(disabledLink3)
        {
        webPageOutput.println("Page 3");
        }
      else
        {
        Routines.WriteHTMLLink(request,
                               response,
                               webPageOutput,
                               "SortPlayers",
                               "page=Page3"+
                               "&league=" + leagueNumber +
                               "&team=" + teamNumber +
                               "&currentPage=" + currentPage +
                               "&currentSkillButton=" + currentSkillsButton +
                               "&positionNumber=" + positionNumber +
                               "&currentPositionNumber=" + positionNumber +
                               "&sortPage=" + sortPage +
                               "&sortSkill=" + sortSkill,
                               "Page 3",
                               "opt",
                               true);
        }
      webPageOutput.println("<B>�</B>");
      if(disabledLink4)
        {
        webPageOutput.println("Page 4");
        }
      else
        {
        Routines.WriteHTMLLink(request,
                               response,
                               webPageOutput,
                               "SortPlayers",
                               "page=Page4"+
                               "&league=" + leagueNumber +
                               "&team=" + teamNumber +
                               "&currentPage=" + currentPage +
                               "&currentSkillButton=" + currentSkillsButton +
                               "&positionNumber=" + positionNumber +
                               "&currentPositionNumber=" + positionNumber +
                               "&sortPage=" + sortPage +
                               "&sortSkill=" + sortSkill,
                               "Page 4",
                               "opt",
                               true);
        }
      webPageOutput.println("<B>�</B>");
      if(disabledLink5)
        {
        webPageOutput.println("Page 5");
        }
      else
        {
        Routines.WriteHTMLLink(request,
                               response,
                               webPageOutput,
                               "SortPlayers",
                               "page=Page5"+
                               "&league=" + leagueNumber +
                               "&team=" + teamNumber +
                               "&currentPage=" + currentPage +
                               "&currentSkillButton=" + currentSkillsButton +
                               "&positionNumber=" + positionNumber +
                               "&currentPositionNumber=" + positionNumber +
                               "&sortPage=" + sortPage +
                               "&sortSkill=" + sortSkill,
                               "Page 5",
                               "opt",
                               true);
        }
      webPageOutput.println("<B>�</B>");
      if(disabledLink6)
        {
        webPageOutput.println("Page 6");
        }
      else
        {
        Routines.WriteHTMLLink(request,
                               response,
                               webPageOutput,
                               "SortPlayers",
                               "page=Page6"+
                               "&league=" + leagueNumber +
                               "&team=" + teamNumber +
                               "&currentPage=" + currentPage +
                               "&currentSkillButton=" + currentSkillsButton +
                               "&positionNumber=" + positionNumber +
                               "&currentPositionNumber=" + positionNumber +
                               "&sortPage=" + sortPage +
                               "&sortSkill=" + sortSkill,
                               "Page 6",
                               "opt",
                               true);
        }
      webPageOutput.println("<B>�</B>");
      if(disabledLink7)
        {
        webPageOutput.println("Page 7");
        }
      else
        {
        Routines.WriteHTMLLink(request,
                               response,
                               webPageOutput,
                               "SortPlayers",
                               "page=Page7"+
                               "&league=" + leagueNumber +
                               "&team=" + teamNumber +
                               "&currentPage=" + currentPage +
                               "&currentSkillButton=" + currentSkillsButton +
                               "&positionNumber=" + positionNumber +
                               "&currentPositionNumber=" + positionNumber +
                               "&sortPage=" + sortPage +
                               "&sortSkill=" + sortSkill,
                               "Page 7",
                               "opt",
                               true);
        }
      webPageOutput.println("<B>�</B>");
      if(disabledLink8)
        {
        webPageOutput.println("Page 8");
        }
      else
        {
        Routines.WriteHTMLLink(request,
                               response,
                               webPageOutput,
                               "SortPlayers",
                               "page=Page8"+
                               "&league=" + leagueNumber +
                               "&team=" + teamNumber +
                               "&currentPage=" + currentPage +
                               "&currentSkillButton=" + currentSkillsButton +
                               "&positionNumber=" + positionNumber +
                               "&currentPositionNumber=" + positionNumber +
                               "&sortPage=" + sortPage +
                               "&sortSkill=" + sortSkill,
                               "Page 8",
                               "opt",
                               true);
        }
      webPageOutput.println("</CENTER>");
      Routines.tableStart(false,webPageOutput);
      Routines.tableDataStart(true,true,true,true,false,0,0,"scoresrow",webPageOutput);
      webPageOutput.println("<SELECT NAME=\"positionNumber\">");
      String selected="";
      for(int currentPosition=0;currentPosition<positions.length;currentPosition++)
         {
         if(positionNumbers[currentPosition]==positionNumber||
            (currentPosition==0&&positionNumber==0))
           {
           selected=" SELECTED";
           positionName=positions[currentPosition];
           positionNumber=positionNumbers[currentPosition];
           }
         else
           {
           selected="";
           }
         webPageOutput.println(" <OPTION" + selected + " VALUE=\"" + positionNumbers[currentPosition] + "\">" + positions[currentPosition]);
         }
      webPageOutput.println("</SELECT>");
      webPageOutput.println("<INPUT TYPE=\"SUBMIT\" NAME=\"action\" VALUE=\"View\">");
      Routines.tableDataEnd(true,false,true,webPageOutput);
      Routines.tableEnd(webPageOutput);
      viewScreen(page,
                 leagueNumber,
                 teamNumber,
                 positionNumber,
                 currentPage,
                 currentSkillsButton,
                 sortPage,
                 sortSkill,
                 positionName,
                 skill1,
                 skill2,
                 skill3,
                 skill4,
                 skill5,
                 session,
                 database,
                 request,
                 response,
                 webPageOutput);
      pool.returnConnection(database);
      Routines.WriteHTMLTail(request,response,webPageOutput);
      }

   private void viewScreen(int page,
                           int leagueNumber,
                           int teamNumber,
                           int positionNumber,
                           int currentPage,
                           int currentSkillButton,
                           int sortPage,
                           int sortSkill,
                           String positionName,
                           String skill1,
                           String skill2,
                           String skill3,
                           String skill4,
                           String skill5,
                           HttpSession session,
                           Connection database,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           PrintWriter webPageOutput)
      {
      String[] titleHeader=Routines.getTitleHeaders(positionNumber,currentSkillButton,context);
      Routines.myTableStart(false,webPageOutput);
      Routines.myTableHeader("Ratings for " + positionName,10,webPageOutput);
      int numOfSkillButtons=Routines.getSkillsButtons(positionNumber,context);
      if(currentSkillButton>numOfSkillButtons)
        {
        currentSkillButton=0;
        }
      boolean disabledSkill1=false;
      boolean disabledSkill2=false;
      boolean disabledSkill3=false;
      boolean disabledSkill4=false;
      boolean disabledSkill5=false;
      boolean skillChange=false;
      if(skill1!=null||
         skill2!=null||
         skill3!=null||
         skill4!=null||
         skill5!=null)
        {
        skillChange=true;
        }
      if(!skillChange)
        {
        if(sortPage==currentSkillButton)
          {
          if(sortSkill==1)
            {
            disabledSkill1=true;
            }
          if(sortSkill==2)
            {
            disabledSkill2=true;
            }
          if(sortSkill==3)
            {
            disabledSkill3=true;
            }
          if(sortSkill==4)
            {
            disabledSkill4=true;
            }
          if(sortSkill==5)
            {
            disabledSkill5=true;
            }
          }
        }
      if(skill1!=null)
        {
        disabledSkill1=true;
        }
      if(skill2!=null)
        {
        disabledSkill2=true;
        }
      if(skill3!=null)
        {
        disabledSkill3=true;
        }
      if(skill4!=null)
        {
        disabledSkill4=true;
        }
      if(skill5!=null)
        {
        disabledSkill5=true;
        }
      if(numOfSkillButtons>0)
        {
        if(positionNumber==0)
          {
          Routines.tableDataStart(true,false,false,true,false,0,3,"bg1",webPageOutput);
          }
        else
          {
          Routines.tableDataStart(true,false,false,true,false,0,2,"bg1",webPageOutput);
          }
        boolean disabledLink1=false;
        boolean disabledLink2=false;
        boolean disabledLink3=false;
        boolean disabledLink4=false;
        if(currentSkillButton==1||currentSkillButton==0)
          {
          disabledLink1=true;
          }
        if(currentSkillButton==2)
          {
          disabledLink2=true;
          }
        if(currentSkillButton==3)
          {
          disabledLink3=true;
          }
        if(currentSkillButton==4)
          {
          disabledLink4=true;
          }
        switch(numOfSkillButtons)
              {
              case 1:
                 Routines.tableDataStart(true,true,false,false,false,0,1,"bg1",webPageOutput);
                 webPageOutput.println("Overall");
                 Routines.tableDataEnd(false,false,false,webPageOutput);
                 Routines.tableDataStart(true,true,false,false,false,0,5,"bg1",webPageOutput);
                 break;
              case 2:
                 Routines.tableDataStart(true,true,false,false,false,0,1,"bg1",webPageOutput);
                 if(disabledLink1)
                   {
                   webPageOutput.println("Overall");
                   }
                 else
                   {
                   Routines.WriteHTMLLink(request,
                                          response,
                                          webPageOutput,
                                          "SortPlayers",
                                          "skills=Overall"+
                                          "&league=" + leagueNumber +
                                          "&team=" + teamNumber +
                                          "&currentPage=" + currentPage +
                                          "&currentSkillButton=" + currentSkillButton +
                                          "&positionNumber=" + positionNumber +
                                          "&currentPositionNumber=" + positionNumber +
                                          "&sortPage=" + sortPage +
                                          "&sortSkill=" + sortSkill,
                                          "Overall",
                                          "bg1",
                                          true);
                    }
                 Routines.tableDataEnd(false,false,false,webPageOutput);
                 Routines.tableDataStart(true,true,false,false,false,0,1,"bg1",webPageOutput);
                 if(disabledLink2)
                   {
                   webPageOutput.println("Skills1");
                   }
                 else
                   {
                   Routines.WriteHTMLLink(request,
                                          response,
                                          webPageOutput,
                                          "SortPlayers",
                                          "skills=Skills1"+
                                          "&league=" + leagueNumber +
                                          "&team=" + teamNumber +
                                          "&currentPage=" + currentPage +
                                          "&currentSkillButton=" + currentSkillButton +
                                          "&positionNumber=" + positionNumber +
                                          "&currentPositionNumber=" + positionNumber +
                                          "&sortPage=" + sortPage +
                                          "&sortSkill=" + sortSkill,
                                          "Skills1",
                                          "bg1",
                                          true);
                    }
                 Routines.tableDataEnd(false,false,false,webPageOutput);
                 Routines.tableDataStart(true,true,false,false,false,0,4,"bg1",webPageOutput);
                 break;
              case 3:
                 Routines.tableDataStart(true,true,false,false,false,0,1,"bg1",webPageOutput);
                 if(disabledLink1)
                   {
                   webPageOutput.println("Overall");
                   }
                 else
                   {
                   Routines.WriteHTMLLink(request,
                                          response,
                                          webPageOutput,
                                          "SortPlayers",
                                          "skills=Overall"+
                                          "&league=" + leagueNumber +
                                          "&team=" + teamNumber +
                                          "&currentPage=" + currentPage +
                                          "&currentSkillButton=" + currentSkillButton +
                                          "&positionNumber=" + positionNumber +
                                          "&currentPositionNumber=" + positionNumber +
                                          "&sortPage=" + sortPage +
                                          "&sortSkill=" + sortSkill,
                                          "Overall",
                                          "bg1",
                                          true);
                    }
                 Routines.tableDataEnd(false,false,false,webPageOutput);
                 Routines.tableDataStart(true,true,false,false,false,0,1,"bg1",webPageOutput);
                 if(disabledLink2)
                   {
                   webPageOutput.println("Skills1");
                   }
                 else
                   {
                   Routines.WriteHTMLLink(request,
                                          response,
                                          webPageOutput,
                                          "SortPlayers",
                                          "skills=Skills1"+
                                          "&league=" + leagueNumber +
                                          "&team=" + teamNumber +
                                          "&currentPage=" + currentPage +
                                          "&currentSkillButton=" + currentSkillButton +
                                          "&positionNumber=" + positionNumber +
                                          "&currentPositionNumber=" + positionNumber +
                                          "&sortPage=" + sortPage +
                                          "&sortSkill=" + sortSkill,
                                          "Skills1",
                                          "bg1",
                                          true);
                    }
                 Routines.tableDataEnd(false,false,false,webPageOutput);
                 Routines.tableDataStart(true,true,false,false,false,0,1,"bg1",webPageOutput);
                 if(disabledLink3)
                   {
                   webPageOutput.println("Skills2");
                   }
                 else
                   {
                   Routines.WriteHTMLLink(request,
                                          response,
                                          webPageOutput,
                                          "SortPlayers",
                                          "skills=Skills2"+
                                          "&league=" + leagueNumber +
                                          "&team=" + teamNumber +
                                          "&currentPage=" + currentPage +
                                          "&currentSkillButton=" + currentSkillButton +
                                          "&positionNumber=" + positionNumber +
                                          "&currentPositionNumber=" + positionNumber +
                                          "&sortPage=" + sortPage +
                                          "&sortSkill=" + sortSkill,
                                          "Skills2",
                                          "bg1",
                                          true);
                    }
                 Routines.tableDataEnd(false,false,false,webPageOutput);
                 Routines.tableDataStart(true,true,false,false,false,0,3,"bg1",webPageOutput);
                 break;
              case 4:
                 Routines.tableDataStart(true,true,false,false,false,0,1,"bg1",webPageOutput);
                 if(disabledLink1)
                   {
                   webPageOutput.println("Overall");
                   }
                 else
                   {
                   Routines.WriteHTMLLink(request,
                                          response,
                                          webPageOutput,
                                          "SortPlayers",
                                          "skills=Overall"+
                                          "&league=" + leagueNumber +
                                          "&team=" + teamNumber +
                                          "&currentPage=" + currentPage +
                                          "&currentSkillButton=" + currentSkillButton +
                                          "&positionNumber=" + positionNumber +
                                          "&currentPositionNumber=" + positionNumber +
                                          "&sortPage=" + sortPage +
                                          "&sortSkill=" + sortSkill,
                                          "Overall",
                                          "bg1",
                                          true);
                    }
                 Routines.tableDataEnd(false,false,false,webPageOutput);
                 Routines.tableDataStart(true,true,false,false,false,0,1,"bg1",webPageOutput);
                 if(disabledLink2)
                   {
                   webPageOutput.println("Skills1");
                   }
                 else
                   {
                   Routines.WriteHTMLLink(request,
                                          response,
                                          webPageOutput,
                                          "SortPlayers",
                                          "skills=Skills1"+
                                          "&league=" + leagueNumber +
                                          "&team=" + teamNumber +
                                          "&currentPage=" + currentPage +
                                          "&currentSkillButton=" + currentSkillButton +
                                          "&positionNumber=" + positionNumber +
                                          "&currentPositionNumber=" + positionNumber +
                                          "&sortPage=" + sortPage +
                                          "&sortSkill=" + sortSkill,
                                          "Skills1",
                                          "bg1",
                                          true);
                    }
                 Routines.tableDataEnd(false,false,false,webPageOutput);
                 Routines.tableDataStart(true,true,false,false,false,0,1,"bg1",webPageOutput);
                 if(disabledLink3)
                   {
                   webPageOutput.println("Skills2");
                   }
                 else
                   {
                   Routines.WriteHTMLLink(request,
                                          response,
                                          webPageOutput,
                                          "SortPlayers",
                                          "skills=Skills2"+
                                          "&league=" + leagueNumber +
                                          "&team=" + teamNumber +
                                          "&currentPage=" + currentPage +
                                          "&currentSkillButton=" + currentSkillButton +
                                          "&positionNumber=" + positionNumber +
                                          "&currentPositionNumber=" + positionNumber +
                                          "&sortPage=" + sortPage +
                                          "&sortSkill=" + sortSkill,
                                          "Skills2",
                                          "bg1",
                                          true);
                    }
                 Routines.tableDataEnd(false,false,false,webPageOutput);
                 Routines.tableDataStart(true,true,false,false,false,0,1,"bg1",webPageOutput);
                 if(disabledLink4)
                   {
                   webPageOutput.println("Skills3");
                   }
                 else
                   {
                   Routines.WriteHTMLLink(request,
                                          response,
                                          webPageOutput,
                                          "SortPlayers",
                                          "skills=Skills3"+
                                          "&league=" + leagueNumber +
                                          "&team=" + teamNumber +
                                          "&currentPage=" + currentPage +
                                          "&currentSkillButton=" + currentSkillButton +
                                          "&positionNumber=" + positionNumber +
                                          "&currentPositionNumber=" + positionNumber +
                                          "&sortPage=" + sortPage +
                                          "&sortSkill=" + sortSkill,
                                          "Skills3",
                                          "bg1",
                                          true);
                    }
                 Routines.tableDataEnd(false,false,false,webPageOutput);
                 Routines.tableDataStart(true,true,false,false,false,0,2,"bg1",webPageOutput);
                 break;
              default:
			     Routines.writeToLog(servletName,"Unexpected number of buttons : " + numOfSkillButtons,false,context);
	      }
        Routines.tableDataEnd(false,false,true,webPageOutput);
        }
      webPageOutput.println("<TR ALIGN=\"left\" CLASS=\"bg1\">");
      webPageOutput.println("<TD ALIGN='center'>No</TD>");
      webPageOutput.println("<TD>Name</TD>");
      if(positionNumber==0)
        {
        webPageOutput.println("<TD ALIGN='center'>Pos</TD>");
        }
      webPageOutput.println("<TD ALIGN='right'>");
      if(!"".equals(titleHeader[0]))
        {
        if(disabledSkill1)
          {
          webPageOutput.println(titleHeader[0]);
          }
        else
          {
          Routines.WriteHTMLLink(request,
                                 response,
                                 webPageOutput,
                                 "SortPlayers",
                                 "skill1=" + titleHeader[0] +
                                 "&league=" + leagueNumber +
                                 "&team=" + teamNumber +
                                 "&currentPage=" + currentPage +
                                 "&currentSkillButton=" + currentSkillButton +
                                 "&positionNumber=" + positionNumber +
                                 "&currentPositionNumber=" + positionNumber +
                                 "&sortPage=" + sortPage +
                                 "&sortSkill=" + sortSkill,
                                 titleHeader[0],
                                 "bg1",
                                 true);
          }
        }
      webPageOutput.println("</TD>");
      webPageOutput.println("<TD ALIGN='right'>");
      if(!"".equals(titleHeader[1]))
        {
        if(disabledSkill2)
          {
          webPageOutput.println(titleHeader[1]);
          }
        else
          {
          Routines.WriteHTMLLink(request,
                                 response,
                                 webPageOutput,
                                 "SortPlayers",
                                 "skill2=" + titleHeader[1] +
                                 "&league=" + leagueNumber +
                                 "&team=" + teamNumber +
                                 "&currentPage=" + currentPage +
                                 "&currentSkillButton=" + currentSkillButton +
                                 "&positionNumber=" + positionNumber +
                                 "&currentPositionNumber=" + positionNumber +
                                 "&sortPage=" + sortPage +
                                 "&sortSkill=" + sortSkill,
                                 titleHeader[1],
                                 "bg1",
                                 true);
          }
        }
      webPageOutput.println("</TD>");
      webPageOutput.println("<TD ALIGN='right'>");
      if(!"".equals(titleHeader[2]))
        {
        if(disabledSkill3)
          {
          webPageOutput.println(titleHeader[2]);
          }
        else
          {
          Routines.WriteHTMLLink(request,
                                 response,
                                 webPageOutput,
                                 "SortPlayers",
                                 "skill3=" + titleHeader[2] +
                                 "&league=" + leagueNumber +
                                 "&team=" + teamNumber +
                                 "&currentPage=" + currentPage +
                                 "&currentSkillButton=" + currentSkillButton +
                                 "&positionNumber=" + positionNumber +
                                 "&currentPositionNumber=" + positionNumber +
                                 "&sortPage=" + sortPage +
                                 "&sortSkill=" + sortSkill,
                                 titleHeader[2],
                                 "bg1",
                                 true);
          }
        }
      webPageOutput.println("</TD>");
      webPageOutput.println("<TD ALIGN='right'>");
      if(!"".equals(titleHeader[3]))
        {
        if(disabledSkill4)
          {
          webPageOutput.println(titleHeader[3]);
          }
        else
          {
          Routines.WriteHTMLLink(request,
                                 response,
                                 webPageOutput,
                                 "SortPlayers",
                                 "skill4=" + titleHeader[3] +
                                 "&league=" + leagueNumber +
                                 "&team=" + teamNumber +
                                 "&currentPage=" + currentPage +
                                 "&currentSkillButton=" + currentSkillButton +
                                 "&positionNumber=" + positionNumber +
                                 "&currentPositionNumber=" + positionNumber +
                                 "&sortPage=" + sortPage +
                                 "&sortSkill=" + sortSkill,
                                 titleHeader[3],
                                 "bg1",
                                 true);
          }
        }
      webPageOutput.println("</TD>");
      webPageOutput.println("<TD ALIGN='right'>");
      if(!"".equals(titleHeader[4]))
        {
        if(disabledSkill5)
          {
          webPageOutput.println(titleHeader[4]);
          }
        else
          {
          Routines.WriteHTMLLink(request,
                                 response,
                                 webPageOutput,
                                 "SortPlayers",
                                 "skill5=" + titleHeader[4] +
                                 "&league=" + leagueNumber +
                                 "&team=" + teamNumber +
                                 "&currentPage=" + currentPage +
                                 "&currentSkillButton=" + currentSkillButton +
                                 "&positionNumber=" + positionNumber +
                                 "&currentPositionNumber=" + positionNumber +
                                 "&sortPage=" + sortPage +
                                 "&sortSkill=" + sortSkill,
                                 titleHeader[4],
                                 "bg1",
                                 true);
          }
        }
      webPageOutput.println("</TD>");
      webPageOutput.println("</TR>");
      boolean playersFound=false;
      int minPlayers=((page-1)*25);
      int maxPlayers=(page*24)+(page-1);
      int currentPlayer=0;
      int rate=1;
      try
        {
        int[] positionSkills=Routines.getNumOfSkills(database,context);
        Statement sql=database.createStatement();
        ResultSet queryResult;
        int unSortedPlayers[][]=new int[200][27];
        currentPlayer=0;
        if(positionNumber==0)
          {
          queryResult=sql.executeQuery("SELECT draftratings.PlayerNumber,players.PositionNumber," +
                                       "Intelligence,Ego,Attitude,(Potential*10),(BurnRate*10)," +
                                       "Skill1,Skill2,Skill3,Skill4,Skill5," +
                                       "Skill6,Skill7,Skill8,Skill9,Skill10," +
                                       "Skill11,Skill12,Skill13,Skill14,Skill15," +
                                       "Skill16,Skill17,Skill18,Skill19,Skill20 " +
                                       "FROM draftratings,players,colleges,positions " +
                                       "WHERE draftratings.TeamNumber=" + teamNumber + " " +
                                       "AND draftratings.PlayerNumber=players.PlayerNumber " +
                                       "AND colleges.CollegeNumber=players.CollegeNumber " +
                                       "AND players.PositionNumber=positions.PositionNumber " +
                                       "ORDER BY OverallRating ASC");
          }
        else
          {
          queryResult=sql.executeQuery("SELECT draftratings.PlayerNumber,players.PositionNumber," +
                                       "Intelligence,Ego,Attitude,(Potential*10),(BurnRate*10)," +
                                       "Skill1,Skill2,Skill3,Skill4,Skill5," +
                                       "Skill6,Skill7,Skill8,Skill9,Skill10," +
                                       "Skill11,Skill12,Skill13,Skill14,Skill15," +
                                       "Skill16,Skill17,Skill18,Skill19,Skill20 " +
                                       "FROM draftratings,players,colleges,positions " +
                                       "WHERE draftratings.TeamNumber=" + teamNumber + " " +
                                       "AND draftratings.PlayerNumber=players.PlayerNumber " +
                                       "AND players.PositionNumber=" + positionNumber + " " +
                                       "AND colleges.CollegeNumber=players.CollegeNumber " +
                                       "AND players.PositionNumber=positions.PositionNumber " +
                                       "ORDER BY PositionRating ASC");
          }
        while(queryResult.next())
             {
             if(currentPlayer<200)
               {
               if(!playersFound)
                 {
                 playersFound=true;
                 }
               int playerNumber=queryResult.getInt(1);
               int tempPositionNumber=queryResult.getInt(2);
               int skills[]=new int[25];
               for(int currentSkill=0;currentSkill<skills.length;currentSkill++)
                  {
                  skills[currentSkill]=queryResult.getInt(3+currentSkill);
                  }
               unSortedPlayers[currentPlayer][0]=playerNumber;
               unSortedPlayers[currentPlayer][1]=tempPositionNumber;
               for(int currentSkill=0;currentSkill<skills.length;currentSkill++)
                  {
                  unSortedPlayers[currentPlayer][2+currentSkill]=skills[currentSkill];
                  }
               currentPlayer++;
               }
             }
        int numOfPositions=0;
        queryResult=sql.executeQuery("SELECT PositionNumber " +
                                     "FROM positions " +
                                     "WHERE Type!=3 " +
                                     "AND RealPosition=1 " +
                                     "ORDER BY PositionNumber DESC");
        if(queryResult.first())
          {
          numOfPositions=queryResult.getInt(1);
          }
        int[] numOfSkills=new int[numOfPositions+1];
        queryResult=sql.executeQuery("SELECT positions.PositionNumber,COUNT(positionskills.Sequence) " +
                                     "FROM positionskills,positions " +
                                     "WHERE Type!=3 " +
                                     "AND RealPosition=1 " +
                                     "AND positions.PositionNumber=positionskills.PositionNumber " +
                                     "GROUP BY PositionNumber " +
                                     "ORDER BY positionskills.Sequence DESC");
        while(queryResult.next())
          {
          int tempPositionNumber=queryResult.getInt(1);
          int numberOfSkills=queryResult.getInt(2);
          numOfSkills[tempPositionNumber]=numberOfSkills;
          }
         int sortedPlayers[][]=Routines.sortPlayers(positionNumber,
                                                    unSortedPlayers,
                                                    numOfSkills,
                                                    sortPage,
                                                    sortSkill,
                                                    database,
                                                    context);
         for(currentPlayer=0;currentPlayer<sortedPlayers.length&&currentPlayer<=maxPlayers;currentPlayer++)
            {
            int skills[]=new int[25];
            for(int currentSkill=0;currentSkill<skills.length;currentSkill++)
               {
               skills[currentSkill]=sortedPlayers[currentPlayer][currentSkill+2];
               }
            int[] displaySkills=Routines.getSkills(skills,positionNumber,positionSkills[sortedPlayers[currentPlayer][1]],currentSkillButton,context);
            int playerNumber=sortedPlayers[currentPlayer][0];
            if(currentPlayer>=minPlayers)
              {
              queryResult=sql.executeQuery("SELECT Surname,Forname,PositionCode " +
                                           "FROM players,colleges,positions " +
                                           "WHERE PlayerNumber=" + playerNumber + " " +
                                           "AND colleges.CollegeNumber=players.CollegeNumber " +
                                           "AND players.PositionNumber=positions.PositionNumber");
              queryResult.first();
              String playerName=queryResult.getString(1) + "," + queryResult.getString(2);
              String positionCode=queryResult.getString(3);
              Routines.tableDataStart(false,false,false,true,false,5,0,"scoresrow",webPageOutput);
              webPageOutput.print(rate);
              Routines.tableDataEnd(false,false,false,webPageOutput);
              if(positionNumber==0)
                {
                Routines.tableDataStart(true,false,false,false,false,30,0,"scoresrow",webPageOutput);
                }
              else
                {
                Routines.tableDataStart(true,false,false,false,false,35,0,"scoresrow",webPageOutput);
                }
              Routines.WriteHTMLLink(request,
                                     response,
                                     webPageOutput,
                                     "wfafl",
                                     "action=viewPlayer" +
                                     "&value=" +
                                     playerNumber,
                                     playerName,
                                     null,
                                     true);
              Routines.tableDataEnd(false,false,false,webPageOutput);
              if(positionNumber==0)
                {
                Routines.tableDataStart(true,false,false,false,false,5,0,"scoresrow",webPageOutput);
                webPageOutput.println(positionCode);
                Routines.tableDataEnd(false,false,false,webPageOutput);
                }
              Routines.tableDataStart(false,false,false,false,false,12,0,"scoresrow",webPageOutput);
              webPageOutput.println("<FONT CLASS=\"rate1\">");
              if(displaySkills[0]!=-1)
                {
                webPageOutput.println(Routines.skillsDescription((displaySkills[0]+5)/10));
                }
              webPageOutput.println("</FONT>");
              Routines.tableDataEnd(false,false,false,webPageOutput);
              Routines.tableDataStart(false,false,false,false,false,12,0,"scoresrow",webPageOutput);
              webPageOutput.println("<FONT CLASS=\"rate1\">");
              if(displaySkills[1]!=-1)
                {
                webPageOutput.println(Routines.skillsDescription((displaySkills[1]+5)/10));
                }
              webPageOutput.println("</FONT>");
              Routines.tableDataEnd(false,false,false,webPageOutput);
              Routines.tableDataStart(false,false,false,false,false,12,0,"scoresrow",webPageOutput);
              webPageOutput.println("<FONT CLASS=\"rate1\">");
              if(displaySkills[2]!=-1)
                {
                webPageOutput.println(Routines.skillsDescription((displaySkills[2]+5)/10));
                }
              webPageOutput.println("</FONT>");
              Routines.tableDataEnd(false,false,false,webPageOutput);
              Routines.tableDataStart(false,false,false,false,false,12,0,"scoresrow",webPageOutput);
              webPageOutput.println("<FONT CLASS=\"rate1\">");
              if(displaySkills[3]!=-1)
                {
                webPageOutput.println(Routines.skillsDescription((displaySkills[3]+5)/10));
                }
              webPageOutput.println("</FONT>");
              Routines.tableDataEnd(false,false,false,webPageOutput);
              Routines.tableDataStart(false,false,false,false,false,12,0,"scoresrow",webPageOutput);
              webPageOutput.println("<FONT CLASS=\"rate1\">");
              if(displaySkills[4]!=-1)
                {
                webPageOutput.println(Routines.skillsDescription((displaySkills[4]+5)/10));
                }
              webPageOutput.println("</FONT>");
              Routines.tableDataEnd(false,false,true,webPageOutput);
              }
            rate++;
            }
        }
      catch(SQLException error)
        {
		Routines.writeToLog(servletName,"Unable to retrieve players : " + error,false,context);	
        }
      if(!playersFound)
        {
        Routines.tableDataStart(true,true,false,true,false,0,10,"bg1",webPageOutput);
        webPageOutput.println("No Players found.");
        Routines.tableDataEnd(false,false,true,webPageOutput);
        }
      Routines.tableEnd(webPageOutput);
      webPageOutput.println(Routines.spaceLines(1));
      Routines.tableStart(false,webPageOutput);
      Routines.tableHeader("Actions",0,webPageOutput);
      Routines.tableDataStart(true,true,true,true,false,0,0,"scoresrow",webPageOutput);
      webPageOutput.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"Return to MyTeam page\" NAME=\"action\">");
      webPageOutput.println("<INPUT TYPE=\"hidden\" NAME=\"jsessionid\" VALUE=\"" + session.getId() + "\">");
      webPageOutput.println("<INPUT TYPE=\"hidden\" NAME=\"league\" VALUE=\"" + leagueNumber + "\">");
      webPageOutput.println("<INPUT TYPE=\"hidden\" NAME=\"team\" VALUE=\"" + teamNumber + "\">");
      webPageOutput.println("<INPUT TYPE=\"hidden\" NAME=\"currentPage\" VALUE=\"" + currentPage + "\">");
      webPageOutput.println("<INPUT TYPE=\"hidden\" NAME=\"currentSkillButton\" VALUE=\"" + currentSkillButton + "\">");
      webPageOutput.println("<INPUT TYPE=\"hidden\" NAME=\"currentPositionNumber\" VALUE=\"" + positionNumber + "\">");
      webPageOutput.println("<INPUT TYPE=\"hidden\" NAME=\"sortPage\" VALUE=\"" + sortPage + "\">");
      webPageOutput.println("<INPUT TYPE=\"hidden\" NAME=\"sortSkill\" VALUE=\"" + sortSkill + "\">");
      webPageOutput.println("</FORM>");
      Routines.tableDataEnd(true,false,true,webPageOutput);
      Routines.tableEnd(webPageOutput);
      webPageOutput.println(Routines.spaceLines(1));
      }
}