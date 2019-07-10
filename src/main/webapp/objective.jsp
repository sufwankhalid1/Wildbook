<%@ page contentType="text/html; charset=utf-8" language="java"
    import="
        org.ecocean.servlet.ServletUtilities,
        org.ecocean.*
    "
%><%

/*
   maybe eventually useful later or hopefully not....
String context = ServletUtilities.getContext(request);
String langCode = ServletUtilities.getLanguageCode(request);

*/

%>
<jsp:include page="header.jsp" flush="true"/>
<script src="javascript/objective.js"></script>


<script type="text/javascript">

/// just to do .... something!
$(document).ready(function() {
    $.ajax({
        url: objectiveUrl,  //defined in objective.js
        type: 'GET',
        complete: function(x) {  //no error handling cuz this is throwaway!  :P
            $('.maincontent').append('<xmp style="font-size: 0.7em;">' + JSON.stringify(x.responseJSON, null, 4) + '</xmp>');
        },
        dataType: 'json'
    });
});

</script>

<div class="container maincontent">

</div>

<jsp:include page="footer.jsp" flush="true"/>

