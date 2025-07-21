{
  "job": {
    "content": [
      {
        "reader": {
          "name": "${dbType}reader",
          "parameter": {
            "username": "${dbUserName}",
            "password": "${dbPasswd}",
            "connection": [
              {
                "querySql": [
                  "${sqlQuery}"
                ],
                "jdbcUrl": [
                  "${jdbcUrl}"
                ]
              }
            ]
          }
        },
        "writer": {
          "name": "txtfilewriter",
          "parameter": {
            "path":"${frontendFilePath}",
            "fileName":"${table}_",
            "writeMode":"append",
            "fileFormat":"csv",
            "encoding":"utf-8",
            "fieldDelimiter":",",
            "nullFormat":"null",
            "header":[
              ${columnHeader}
            ]
          }
        }
      }
    ],
    "setting": {
      "speed": {
        "channel": 3
      }
    }
  }
}