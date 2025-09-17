FROM golang:1.24 AS build

WORKDIR /go/src/app
COPY . .

# Note: If 'go mod download' fails due to TLS certificate issues,
# you can use 'go mod vendor' before building and add '-mod=vendor' 
# to the go vet, go test, and go build commands below.
RUN go mod download
RUN go vet -v
RUN go test -v

RUN CGO_ENABLED=0 go build -o /go/bin/app ./cmd/lamp-control-api

FROM gcr.io/distroless/static-debian12

COPY --from=build /go/bin/app /
CMD ["/app"]