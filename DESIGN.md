# DESIGN — <problem name>


## 1. Problem, restated
Built a recommendation carousel feature for something like jio hotstar to show relevant shows recommendation to user based on there preference/behaviour
## 2. Assumptions
recommendation service is out of scope of this design, we can assume that it provides a list based on user context shared by our service

## 3. Scope
**In (must work end to end, demoable):**
API endpoint to expose a recommendation list, to be consumsed by the UI to render casoursel of recommended shows
A stub adaptor to call various recommendation endpoints/models to get list of recommended shows based on user context, basically a adaptor layer
A layer/service which takes the shows id served by recommendation service and fetchs the content to populate our API response
content service will be out ot scope we will use a stub to fetch content based on the ids.


## 4. Approach options
We will expose an endpoint taking user context as input and return list of recommednation shows.
The COntroller will handle the endpoint
A dedicated Service to handle the response population
A dedicated srervice/adaptor layer to call the recommedation servcies, which will guve the list to outr core business servcie sitting behind the controller
The business layer will also have a HAS a relation with content service interface to fetch content based on teh list of ids
Post taht we populate the response and return to teh controller to send back to the client.


## 5. Interfaces (the contract — write this before any code)

### API

Endpoint to serve the client
```
GET /recommendations?locale=<locale>
```
user id can can be resolved from a token

Respoonse: 
```
{
  "recommendations": [
    {
      "id": "<show_id>",
      "title": "<show_title>",
      "description": "<show_description>",
      "image_url": "<show_image_url>"
    },
    ...
  ]
}
```


Endpoint to call recommendation service
GET /recommendation-service

again user id can be resolved from a token and locale can be passed as query param

Response 
```
{
  "recommendations": [
    {
      "id": "<show_id>"
    },
    ...
  ]
}
```

Endpoint to call content service
GET /content?ids=<comma_separated_show_ids>

Response 
```
{
  "content": [
    {
      "id": "<show_id>",
      "title": "<show_title>",
      "description": "<show_description>",
      "image_url": "<show_image_url>"
    },
    ...
  ]
}
```

### Data Models

Show Model
```
{
  "id": "<show_id>",
  "title": "<show_title>",
  "description": "<show_description>",
  "image_url": "<show_image_url>"
}
```


Classes and Interfaces

- Controller: RecommendationController
- Recommendation Interface: IRecommendationService
- Service: RecommendationService
- Service Interface: IRecommendationService
- Adaptor: RecommendationAdaptor
- Content Service: ContentService
- Content Interface: IContentService

