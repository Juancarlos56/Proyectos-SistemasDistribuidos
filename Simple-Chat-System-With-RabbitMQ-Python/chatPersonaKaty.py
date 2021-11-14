import pika
import time
from threading import Thread
from tkinter import *
from tkinter import messagebox

def receiver():

    def llamada(ch, method, propreties, body):
        msg_list.insert(END, "Juan:   "+ body.decode())
    
    connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
    channel = connection.channel()
    channel.queue_declare(queue='task_queue1')

    if (channel.basic_consume(queue='task_queue1', on_message_callback= llamada, auto_ack=True)):
        msg_list.insert(END,"Conectando... ")
        time.sleep(2)
        messagebox.showinfo(" Notificación", "El chat está activo")
        msg_list.delete(0,END)

    channel.start_consuming()
    connection.close()

def send():
    connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
    channel = connection.channel()
    channel.queue_declare(queue='task_queue2')

    live = entry_field.get()
    msg_list.insert(END, "Usted:   " + live)
    
    channel.basic_publish(exchange='', routing_key='task_queue2', body= live)    
    connection.close()

# instancia da TK (janela)

janela = Tk()
janela.title("Ventana Chat Persona Katy")
janela.geometry("350x275+700+100")

# BOTON SALIR PARA DESCONECTAR  
def salir():
    msg_list.delete(0,END)
    janela.destroy()
    
boton_salir = Button(janela, text = "Salir", command= salir )
boton_salir.pack(side = TOP, anchor = NE, pady = 5, padx = 5)

# Muestra texto en pantalla 

messages_frame = Frame(janela)       
scrollbar = Scrollbar(messages_frame) 
msg_list = Listbox(messages_frame, height=10, width=50, yscrollcommand=scrollbar.set)
scrollbar.pack(side= RIGHT, fill=Y)
msg_list.pack(side=LEFT, fill=BOTH)
msg_list.pack()
messages_frame.pack()

# Reciba los mensajes de usuario con cuadro de texto

boton_frame = Frame(janela)
lb = Label(boton_frame,text = "Escriba un mensaje: ")
lb.pack(side = LEFT, anchor= S, padx = 5)
entry_field = Entry(boton_frame, textvariable = '')
#entry_field.bind("<Return>", send)
entry_field.pack(side = LEFT, anchor = SE,pady=  5)

#Botones, enviar

send_button = Button(boton_frame, text= "Enviar", command= send)
send_button.pack(side = LEFT, anchor = S, pady = 5, padx = 5)

boton_frame.pack()

receive_thread = Thread(target= receiver)
sender_thread = Thread(target= send)
receive_thread.start()
sender_thread.start()
janela.mainloop()