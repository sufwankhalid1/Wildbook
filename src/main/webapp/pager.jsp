<script src="javascript/underscore-min.js"></script>
<script src="javascript/backbone-min.js"></script>
<script src="javascript/core.js"></script>
<script src="javascript/classes/Base.js"></script>
<script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDz5Pgz2NCjFkss9AJwxqFjejPhxJrOj-M"></script>
<script src="javascript/pager.js"></script>

<jsp:include page="headerfull.jsp" flush="true"/>

<script>
$(document).ready(function() {
    wildbook.init(function(){
        pager.show('<%=request.getParameter("page") %>');
    });
});
</script>

<div id="pages"></div>

<jsp:include page="footerfull.jsp" flush="true"/>
