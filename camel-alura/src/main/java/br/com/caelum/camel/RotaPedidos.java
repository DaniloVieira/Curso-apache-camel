package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.function.Supplier;

public class RotaPedidos {

	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		Supplier<RouteBuilder> camelFileName = () -> new RouteBuilder() {
			@Override
			public void configure() throws Exception {

				from("file:pedidos?delay=5s&noop=true").
						setProperty("pedidoId", xpath("/pedido/id/text()")).
						setProperty("clienteId", xpath("/pedido/pagamento/titular/text()")).
						split().
							xpath("/pedido/itens/item").
						filter().
							xpath("item/formato[text()='EBOOK']").
						setProperty("ebookId", xpath("/item/livro/codigo/text()")).
						marshal().xmljson().
						log("${id} \n ${body}").
//							setHeader(Exchange.FILE_NAME, simple("${file:name.noext}-${header.CamelSplitIndex}.json")).
//							setHeader(Exchange.HTTP_METHOD, HttpMethods.POST).
//							setHeader(Exchange.HTTP_METHOD, HttpMethods.GET).
							setHeader(Exchange.HTTP_QUERY, simple("ebookId=${property.ebookId}&pedidoId=${property.pedidoId}&clienteId=${property.clienteId}")).
//						to("file:saida");
						to("http4://localhost:8080/webservices/ebook/item");
			}
		};
		context.addRoutes(camelFileName.get());
		context.start();
		Thread.sleep(6000);
		context.stop();
	}	
}
