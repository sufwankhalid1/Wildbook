<jsp:include page="headerfull.jsp" flush="true"/>

<script src="javascript/underscore-min.js"></script>
<script src="javascript/backbone-min.js"></script>
<script src="javascript/core.js"></script>
<script src="javascript/classes/Base.js"></script>

<script>
$(document).ready(function() {
    wildbook.init(function(){
        pager.show('<%=request.getParameter("page") %>');
    });
});
</script>

<jsp:include page="footerfull.jsp" flush="true"/>
