package exoplatformdatabase.calendar

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
class CalendarAddTaskSimulation extends Simulation {
       val calendarDataset = csv("calendar.csv")
	   val httpProtocol = http
		.baseURL("http://localhost:7070")
		
	

	val scn = scenario("CalendarAddTaskSimulation")

			.feed(calendarDataset)
			.exec(http("Login")
				.post("/portal/login")
				.formParam("initialURI", "/portal/intranet/")
				.formParam("username", "${userName}")
				.formParam("password", "${userPassword}")
				.formParam("rememberme", "true")
		        .check(status.is(200)))
			.exec(http("Goto Calendar Home")
				.get("/portal/intranet/calendar")
				.check(status.is(200))
				.check(regex("""gtn:csrf.*value="([^"]*)"""").saveAs("gtncsrf"))
				.check(regex("""UIQuickAddTask.*\?portal:componentId=([^&]+)(&amp;|&)interactionstate=([^&]+)(&amp;|&).*""").saveAs("addTask_componentId"))
				.check(regex("""UIQuickAddTask.*interactionstate=([^&]+)(&amp;|&).*""").saveAs("addTask_interactionstate")))
	.exec(http("Add new event")
			.post("/portal/intranet/calendar?portal:componentId=${addTask_componentId}&interactionstate=${addTask_interactionstate}&portal:type=action&ajaxRequest=true")
			.formParam("formOp", "Save")
			.formParam("gtn:csrf", "${gtncsrf}")
			.formParam("eventName", "${eventName}")
			.formParam("description", "${description}")
			.formParam("from", "${fromDate}")
			.formParam("fromTime", "${fromTime}")
			.formParam("to", "${toDate}")
			.formParam("toTime", "${toTime}")
			.formParam("calendar", "${calendarGroup}")
			.formParam("category", "${eventCategory}"))
	.exec(session => {
  // print the Session for debugging, don't do that on real Simulations
  println(session)
  session
})

	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}