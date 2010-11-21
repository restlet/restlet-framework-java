<html>
<head>
<title>Example mail</title>
</head>
<body>
<form action="?method=PUT" method="POST">
<table>
    <tbody>
        <tr>
            <td>Status</td>
            <td>${status}</td>
        </tr>
        <tr>
            <td>Subject</td>
            <td><input type="text" name="subject" size="80" value="${subject}"></td>
        </tr>
        <tr>
            <td>Content</td>
            <td><textarea name="content" rows="10" cols="80">${content}</textarea></td>
        </tr>
        <tr>
            <td/>
            <td><input type="submit" value="Save"></td>
        </tr>
    </tbody>
</table>
</form>
</body>
</html>
