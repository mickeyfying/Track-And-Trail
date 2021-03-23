package androidx.lifecycle;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ClassesInfoCache {
    private static final int CALL_TYPE_NO_ARG = 0;
    private static final int CALL_TYPE_PROVIDER = 1;
    private static final int CALL_TYPE_PROVIDER_WITH_EVENT = 2;
    static ClassesInfoCache a = new ClassesInfoCache();
    private final Map<Class, CallbackInfo> mCallbackMap = new HashMap();
    private final Map<Class, Boolean> mHasLifecycleMethods = new HashMap();

    static class CallbackInfo {
        final Map<Lifecycle.Event, List<MethodReference>> a = new HashMap();
        final Map<MethodReference, Lifecycle.Event> b;

        CallbackInfo(Map<MethodReference, Lifecycle.Event> map) {
            this.b = map;
            for (Map.Entry next : map.entrySet()) {
                Lifecycle.Event event = (Lifecycle.Event) next.getValue();
                List list = this.a.get(event);
                if (list == null) {
                    list = new ArrayList();
                    this.a.put(event, list);
                }
                list.add(next.getKey());
            }
        }

        private static void invokeMethodsForEvent(List<MethodReference> list, LifecycleOwner lifecycleOwner, Lifecycle.Event event, Object obj) {
            if (list != null) {
                for (int size = list.size() - 1; size >= 0; size--) {
                    list.get(size).a(lifecycleOwner, event, obj);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void a(LifecycleOwner lifecycleOwner, Lifecycle.Event event, Object obj) {
            invokeMethodsForEvent(this.a.get(event), lifecycleOwner, event, obj);
            invokeMethodsForEvent(this.a.get(Lifecycle.Event.ON_ANY), lifecycleOwner, event, obj);
        }
    }

    static class MethodReference {
        final int a;
        final Method b;

        MethodReference(int i, Method method) {
            this.a = i;
            this.b = method;
            this.b.setAccessible(true);
        }

        /* access modifiers changed from: package-private */
        public void a(LifecycleOwner lifecycleOwner, Lifecycle.Event event, Object obj) {
            try {
                int i = this.a;
                if (i == 0) {
                    this.b.invoke(obj, new Object[0]);
                } else if (i == 1) {
                    this.b.invoke(obj, new Object[]{lifecycleOwner});
                } else if (i == 2) {
                    this.b.invoke(obj, new Object[]{lifecycleOwner, event});
                }
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Failed to call observer method", e.getCause());
            } catch (IllegalAccessException e2) {
                throw new RuntimeException(e2);
            }
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || MethodReference.class != obj.getClass()) {
                return false;
            }
            MethodReference methodReference = (MethodReference) obj;
            return this.a == methodReference.a && this.b.getName().equals(methodReference.b.getName());
        }

        public int hashCode() {
            return (this.a * 31) + this.b.getName().hashCode();
        }
    }

    ClassesInfoCache() {
    }

    private CallbackInfo createInfo(Class cls, @Nullable Method[] methodArr) {
        int i;
        CallbackInfo a2;
        Class superclass = cls.getSuperclass();
        HashMap hashMap = new HashMap();
        if (!(superclass == null || (a2 = a(superclass)) == null)) {
            hashMap.putAll(a2.b);
        }
        for (Class a3 : cls.getInterfaces()) {
            for (Map.Entry next : a(a3).b.entrySet()) {
                verifyAndPutHandler(hashMap, (MethodReference) next.getKey(), (Lifecycle.Event) next.getValue(), cls);
            }
        }
        if (methodArr == null) {
            methodArr = getDeclaredMethods(cls);
        }
        boolean z = false;
        for (Method method : methodArr) {
            OnLifecycleEvent onLifecycleEvent = (OnLifecycleEvent) method.getAnnotation(OnLifecycleEvent.class);
            if (onLifecycleEvent != null) {
                Class[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length <= 0) {
                    i = 0;
                } else if (parameterTypes[0].isAssignableFrom(LifecycleOwner.class)) {
                    i = 1;
                } else {
                    throw new IllegalArgumentException("invalid parameter type. Must be one and instanceof LifecycleOwner");
                }
                Lifecycle.Event value = onLifecycleEvent.value();
                if (parameterTypes.length > 1) {
                    if (!parameterTypes[1].isAssignableFrom(Lifecycle.Event.class)) {
                        throw new IllegalArgumentException("invalid parameter type. second arg must be an event");
                    } else if (value == Lifecycle.Event.ON_ANY) {
                        i = 2;
                    } else {
                        throw new IllegalArgumentException("Second arg is supported only for ON_ANY value");
                    }
                }
                if (parameterTypes.length <= 2) {
                    verifyAndPutHandler(hashMap, new MethodReference(i, method), value, cls);
                    z = true;
                } else {
                    throw new IllegalArgumentException("cannot have more than 2 params");
                }
            }
        }
        CallbackInfo callbackInfo = new CallbackInfo(hashMap);
        this.mCallbackMap.put(cls, callbackInfo);
        this.mHasLifecycleMethods.put(cls, Boolean.valueOf(z));
        return callbackInfo;
    }

    private Method[] getDeclaredMethods(Class cls) {
        try {
            return cls.getDeclaredMethods();
        } catch (NoClassDefFoundError e) {
            throw new IllegalArgumentException("The observer class has some methods that use newer APIs which are not available in the current OS version. Lifecycles cannot access even other methods so you should make sure that your observer classes only access framework classes that are available in your min API level OR use lifecycle:compiler annotation processor.", e);
        }
    }

    private void verifyAndPutHandler(Map<MethodReference, Lifecycle.Event> map, MethodReference methodReference, Lifecycle.Event event, Class cls) {
        Lifecycle.Event event2 = map.get(methodReference);
        if (event2 != null && event != event2) {
            Method method = methodReference.b;
            throw new IllegalArgumentException("Method " + method.getName() + " in " + cls.getName() + " already declared with different @OnLifecycleEvent value: previous value " + event2 + ", new value " + event);
        } else if (event2 == null) {
            map.put(methodReference, event);
        }
    }

    /* access modifiers changed from: package-private */
    public CallbackInfo a(Class cls) {
        CallbackInfo callbackInfo = this.mCallbackMap.get(cls);
        return callbackInfo != null ? callbackInfo : createInfo(cls, (Method[]) null);
    }

    /* access modifiers changed from: package-private */
    public boolean b(Class cls) {
        Boolean bool = this.mHasLifecycleMethods.get(cls);
        if (bool != null) {
            return bool.booleanValue();
        }
        Method[] declaredMethods = getDeclaredMethods(cls);
        for (Method annotation : declaredMethods) {
            if (((OnLifecycleEvent) annotation.getAnnotation(OnLifecycleEvent.class)) != null) {
                createInfo(cls, declaredMethods);
                return true;
            }
        }
        this.mHasLifecycleMethods.put(cls, false);
        return false;
    }
}
